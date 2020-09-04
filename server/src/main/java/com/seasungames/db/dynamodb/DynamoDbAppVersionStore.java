package com.seasungames.db.dynamodb;

import com.fasterxml.jackson.core.type.TypeReference;
import com.seasungames.config.DbConfig;
import com.seasungames.db.AppVersionStore;
import com.seasungames.db.pojo.AppVersionItem;
import com.seasungames.db.pojo.AppVersionItems;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.jackson.JacksonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.util.*;

import static com.seasungames.util.FunctionUtils.pcall;

/**
 * Created by jianghaitao on 2020/4/27.
 */
@ApplicationScoped
public class DynamoDbAppVersionStore implements AppVersionStore {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbAppVersionStore.class);
    private static final String HASH_KEY_ID = "id";
    private static final String RANGE_KEY_ID = "version";
    private static final String ATTRIBUTE_DESC = "desc";
    private static final String ATTRIBUTE_URL = "url";
    private static final String ATTRIBUTE_CREATE_TIME = "ctime";
    private static final String ATTRIBUTE_MORE_URLS = "moreUrls";
    private static final String TTL = "ttl";
    // 存储成同一个值，为了按时间分页查询, key是gid,值也是gid
    private static final String GSI_HASH_KEY = "gid";
    private static final String GSI_CREATE_TIME_INDEX_NAME = "GsiCtimeIndex";

    private static final TypeReference<Map<String, String>> ATTRIBUTE_MORE_URLS_TYPE = new TypeReference<Map<String, String>>() {
    };

    @Inject
    Vertx vertx;

    @Inject
    DbConfig config;

    @Inject
    DynamoDbAsyncClient client;

    private String tableName;

    private int ttlSeconds;

    @PostConstruct
    void init() {
        tableName = config.appVersionsTableName();
        ttlSeconds = config.ttlDays() * 24 * 60 * 60;
    }

    @Override
    public void createTable(Handler<AsyncResult<Void>> resultHandle) {

        var builder = CreateTableRequest.builder()
                .tableName(tableName)
                .keySchema(getKeySchema())
                .attributeDefinitions(getAttributeDefinitions())
                .globalSecondaryIndexes(getGlobalSecondaryIndexes());

        DynamoDbCreateTable.builder()
                .client(client).config(config).vertx(vertx)
                .tableName(tableName).ttlName(TTL)
                .build()
                .createTable(builder, resultHandle, true);

    }

    @Override
    public void save(AppVersionItem appVersionItem, Handler<AsyncResult<Boolean>> resultHandler) {
        Objects.requireNonNull(appVersionItem, "appVersionItem");
        Objects.requireNonNull(resultHandler, "resultHandler");
        var request = PutItemRequest.builder()
                .tableName(tableName)
                .conditionExpression(getConditionExpressionForSave())
                .item(toItem(appVersionItem))
                .build();

        client.putItem(request).whenComplete((response, err) ->
                pcall(() ->
                        Optional.ofNullable(response).ifPresentOrElse(res -> resultHandler.handle(Future.succeededFuture(true)),
                                () -> {
                                    if (err.getCause() instanceof ConditionalCheckFailedException) {
                                        resultHandler.handle(Future.succeededFuture(false));
                                    } else {
                                        resultHandler.handle(Future.failedFuture(err));
                                    }
                                })
                ));
    }

    private static String getConditionExpressionForSave() {
        return "attribute_not_exists(" + HASH_KEY_ID + ") AND attribute_not_exists(" + RANGE_KEY_ID + ")";
    }

    @Override
    public void get(String id, String version, Handler<AsyncResult<Optional<AppVersionItem>>> resultHandler) {
        final var getItemRequest = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(HASH_KEY_ID, AttributeValue.builder().s(id).build(),
                        RANGE_KEY_ID, AttributeValue.builder().s(version).build()))
                .consistentRead(true)
                .build();

        client.getItem(getItemRequest).whenComplete((response, err) ->
                pcall(() -> {
                    if (response != null) {
                        resultHandler.handle(Future.succeededFuture(Optional.ofNullable(from(response.item()))));
                    } else {
                        resultHandler.handle(Future.failedFuture(err));
                    }
                }));
    }

    @Override
    public void getByLimit(String id, long ctime, int limit, Handler<AsyncResult<AppVersionItems>> resultHandler) {
        Objects.requireNonNull(resultHandler, "resultHandler");
        getByLimit(id, ctime, limit, false, ar -> {
            if (ar.succeeded()) {
                var hasMore = ar.result().hasLastEvaluatedKey();
                resultHandler.handle(Future.succeededFuture(getAppVersionItems(froms(ar.result().items()), hasMore)));
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    private void getByLimit(String id, long ctime, int limit, boolean scanIndexForward, Handler<AsyncResult<QueryResponse>> resultHandler) {
        Objects.requireNonNull(resultHandler, "resultHandler");
        String keyConditionExpression = "#key = :key and #ctime < :ctime";
        if (scanIndexForward) {
            keyConditionExpression = "#key = :key and #ctime > :ctime";
        }
        Map<String, String> expressionAttributeNames = Map.of("#ctime", ATTRIBUTE_CREATE_TIME, "#key", GSI_HASH_KEY);
        Map<String, AttributeValue> expressionAttributeValues = Map.of(":ctime", AttributeValue.builder().n(String.valueOf(ctime)).build(),
                ":key", AttributeValue.builder().s(id).build());
        getByLimit(limit, scanIndexForward, keyConditionExpression, expressionAttributeNames, expressionAttributeValues, ar -> {
            if (ar.succeeded()) {
                resultHandler.handle(Future.succeededFuture(ar.result()));
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    private void getByLimit(int limit, boolean scanIndexForward, String keyConditionExpression, Map<String, String> expressionAttributeNames,
                            Map<String, AttributeValue> expressionAttributeValues, Handler<AsyncResult<QueryResponse>> resultHandler) {
        Objects.requireNonNull(resultHandler, "resultHandler");
        var request = QueryRequest.builder().tableName(tableName)
                .indexName(GSI_CREATE_TIME_INDEX_NAME)
                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                .keyConditionExpression(keyConditionExpression)
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .scanIndexForward(scanIndexForward)
                .limit(limit).build();

        client.query(request).whenComplete((response, err) ->
                pcall(() -> {
                    if (response != null) {
                        resultHandler.handle(Future.succeededFuture(response));
                    } else {
                        resultHandler.handle(Future.failedFuture(err));
                    }
                }));
    }

    @Override
    public void getLast(String id, Handler<AsyncResult<Optional<AppVersionItem>>> resultHandler) {
        getByLimit(id, Instant.now().getEpochSecond(), 1, false, ar -> {
            if (ar.succeeded()) {
                resultHandler.handle(Future.succeededFuture(getFirst(froms(ar.result().items()))));
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void addUrl(AppVersionItem appVersionItem, Handler<AsyncResult<Void>> resultHandler) {
        Objects.requireNonNull(appVersionItem, "appVersionItem");
        Objects.requireNonNull(resultHandler, "resultHandler");
        var request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(HASH_KEY_ID, AttributeValue.builder().s(appVersionItem.id()).build(),
                        RANGE_KEY_ID, AttributeValue.builder().s(appVersionItem.version()).build()))
                .conditionExpression(getConditionExpressionForUpdate())
                .updateExpression("set moreUrls=:moreUrls")
                .expressionAttributeValues(Map.of(
                        ":moreUrls", AttributeValue.builder().s(Json.encode(appVersionItem.moreUrls())).build()))
                .build();


        client.updateItem(request).whenComplete((response, err) ->
                pcall(() ->
                        Optional.ofNullable(response).ifPresentOrElse(res -> resultHandler.handle(Future.succeededFuture()),
                                () -> {
                                    if (err.getCause() instanceof ConditionalCheckFailedException) {
                                        resultHandler.handle(Future.succeededFuture());
                                    } else {
                                        resultHandler.handle(Future.failedFuture(err));
                                    }
                                })
                ));
    }

    private static String getConditionExpressionForUpdate() {
        return "attribute_exists(" + HASH_KEY_ID + ")";
    }

    private static Optional<AppVersionItem> getFirst(List<AppVersionItem> appItems) {
        if (Objects.isNull(appItems) || appItems.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(appItems.get(0));
    }

    private static AppVersionItems getAppVersionItems(List<AppVersionItem> appItems, boolean hasMore) {
        return AppVersionItems.builder().items(appItems).hasMore(hasMore).build();
    }

    private Collection<KeySchemaElement> getKeySchema() {
        return List.of(KeySchemaElement.builder()
                        .attributeName(HASH_KEY_ID)
                        .keyType(KeyType.HASH)
                        .build(),
                KeySchemaElement.builder()
                        .attributeName(RANGE_KEY_ID)
                        .keyType(KeyType.RANGE)
                        .build());
    }

    private Collection<AttributeDefinition> getAttributeDefinitions() {
        return List.of(AttributeDefinition.builder()
                        .attributeName(HASH_KEY_ID)
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                AttributeDefinition.builder()
                        .attributeName(RANGE_KEY_ID)
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                AttributeDefinition.builder()
                        .attributeName(GSI_HASH_KEY)
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                AttributeDefinition.builder()
                        .attributeName(ATTRIBUTE_CREATE_TIME)
                        .attributeType(ScalarAttributeType.N)
                        .build());
    }

    private List<GlobalSecondaryIndex> getGlobalSecondaryIndexes() {
        return List.of(GlobalSecondaryIndex.builder()
                .indexName(GSI_CREATE_TIME_INDEX_NAME)
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName(GSI_HASH_KEY)
                                .keyType(KeyType.HASH)
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName(ATTRIBUTE_CREATE_TIME)
                                .keyType(KeyType.RANGE)
                                .build())
                .projection(Projection.builder()
                        .projectionType(ProjectionType.ALL)
                        .build())
                .build());
    }

    private static List<AppVersionItem> froms(List<Map<String, AttributeValue>> items) {
        if (Objects.isNull(items) || items.isEmpty()) {
            return Collections.emptyList();
        }

        var list = new ArrayList<AppVersionItem>(items.size());
        for (Map<String, AttributeValue> item : items) {
            list.add(from(item));
        }
        return list;
    }

    private static AppVersionItem from(Map<String, AttributeValue> item) {
        if (Objects.isNull(item) || item.size() == 0) {
            return null;
        }
        var builder = AppVersionItem.builder();

        builder.id(item.get(HASH_KEY_ID).s())
                .version(item.get(RANGE_KEY_ID).s())
                .url(item.get(ATTRIBUTE_URL).s())
                .time(Long.parseLong(item.get(ATTRIBUTE_CREATE_TIME).n()))
                .ttl(Long.parseLong(item.get(TTL).n()));
        if (item.containsKey(ATTRIBUTE_DESC)) {
            builder.desc(item.get(ATTRIBUTE_DESC).s());
        }

        if (item.containsKey(ATTRIBUTE_MORE_URLS)) {
            var moreUrlStr = item.get(ATTRIBUTE_MORE_URLS).s();
            builder.moreUrls(JacksonCodec.decodeValue(moreUrlStr, ATTRIBUTE_MORE_URLS_TYPE));
        }
        return builder.build();
    }

    private Map<String, AttributeValue> toItem(AppVersionItem item) {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put(HASH_KEY_ID, AttributeValue.builder().s(item.id()).build());
        map.put(RANGE_KEY_ID, AttributeValue.builder().s(item.version()).build());
        var desc = item.desc();
        if (Objects.nonNull(desc) && !desc.isEmpty()) {
            map.put(ATTRIBUTE_DESC, AttributeValue.builder().s(item.desc()).build());
        }
        map.put(ATTRIBUTE_URL, AttributeValue.builder().s(item.url()).build());
        map.put(GSI_HASH_KEY, AttributeValue.builder().s(item.id()).build());
        map.put(ATTRIBUTE_CREATE_TIME, AttributeValue.builder().n(Long.toString(item.time())).build());
        map.put(TTL, AttributeValue.builder().n(Long.toString(item.ttl() + ttlSeconds)).build());
        return map;
    }
}
