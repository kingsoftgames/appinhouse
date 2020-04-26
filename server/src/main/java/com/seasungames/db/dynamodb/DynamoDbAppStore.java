package com.seasungames.db.dynamodb;

import com.seasungames.config.DbConfig;
import com.seasungames.db.AppStore;
import com.seasungames.db.pojo.AppItem;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

import static com.seasungames.util.FunctionUtils.pcall;

/**
 * Created by jianghaitao on 2020/4/21.
 */

@ApplicationScoped
public class DynamoDbAppStore implements AppStore {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbAppStore.class);
    private static final String HASH_KEY_ID = "app";
    private static final String ATTRIBUTE_DESCRIPTION = "description";
    private static final String ATTRIBUTE_CREATE_TIME = "ctime";
    private static final String ATTRIBUTE_ALIAS = "alias";
    // 存储成同一个值，为了分页查询, key是gid,值也是gid
    private static final String GSI_HASH_KEY = "gid";
    private static final String GSI_CREATE_TIME_INDEX_NAME = "GsiCtimeIndex";

    @Inject
    Vertx vertx;

    @Inject
    DbConfig config;

    @Inject
    DynamoDbAsyncClient client;

    private String tableName;

    @PostConstruct
    void init() {
        tableName = config.appsTableName();
    }

    @Override
    public void save(AppItem appItem, Handler<AsyncResult<Boolean>> resultHandler) {
        Objects.requireNonNull(appItem, "appItem");
        Objects.requireNonNull(resultHandler, "resultHandler");
        var request = PutItemRequest.builder()
                .tableName(tableName)
                .item(toItem(appItem))
                .conditionExpression(getConditionExpressionForSave())
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

    private String getConditionExpressionForSave() {
        return "attribute_not_exists(" + HASH_KEY_ID + ") and attribute_not_exists(" + HASH_KEY_ID + ")";
    }

    @Override
    public void exist(String app, Handler<AsyncResult<Boolean>> resultHandler) {

        Future.<AppItem>future(f ->
                get(app, f)
        ).compose(result ->
                Future.<Boolean>future(p -> p.complete(Objects.nonNull(result)))
        ).setHandler(resultHandler);
    }

    @Override
    public void getByLimit(long ctime, int limit, Handler<AsyncResult<List<AppItem>>> resultHandler) {
        Objects.requireNonNull(resultHandler, "resultHandler");
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        var request = QueryRequest.builder().tableName(tableName)
                .indexName(GSI_CREATE_TIME_INDEX_NAME)
                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                .keyConditionExpression("#key = :key and #ctime < :ctime")
                .expressionAttributeNames(Map.of("#ctime", ATTRIBUTE_CREATE_TIME, "#key", GSI_HASH_KEY))
                .expressionAttributeValues(Map.of(":ctime", AttributeValue.builder().n(String.valueOf(ctime)).build(),
                        ":key", AttributeValue.builder().s(GSI_HASH_KEY).build()))
                .scanIndexForward(false)
                .limit(limit).build();

        client.query(request).whenComplete((response, err) ->
                pcall(() -> {
                    if (response != null) {
                        resultHandler.handle(Future.succeededFuture(froms(response.items())));
                    } else {
                        resultHandler.handle(Future.failedFuture(err));
                    }
                }));
    }

    @Override
    public void get(String app, Handler<AsyncResult<AppItem>> resultHandler) {
        final var getItemRequest = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(HASH_KEY_ID, AttributeValue.builder().s(app).build()))
                .consistentRead(true)
                .build();

        client.getItem(getItemRequest).whenComplete((response, err) ->
                pcall(() -> {
                    if (response != null) {
                        resultHandler.handle(Future.succeededFuture(from(response.item())));
                    } else {
                        resultHandler.handle(Future.failedFuture(err));
                    }
                }));
    }

    @Override
    public void delete(String app, Handler<AsyncResult<Void>> resultHandler) {
        Objects.requireNonNull(resultHandler, "resultHandler");
        var request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(HASH_KEY_ID, AttributeValue.builder().s(app).build()))
                .build();

        client.deleteItem(request).whenComplete((response, err) ->
                pcall(() -> {
                    if (response != null) {
                        resultHandler.handle(Future.succeededFuture());
                    } else {
                        resultHandler.handle(Future.failedFuture(err));
                    }
                }));
    }

    @Override
    public void change(String source, String target, Handler<AsyncResult<Void>> resultHandler) {
        Objects.requireNonNull(source, "target");
        Objects.requireNonNull(resultHandler, "resultHandler");
        Future.<List<AppItem>>future(f ->
                batchGetForChange(source, target, f)
        ).compose(result ->
                Future.<Void>future(f ->
                        batchWriteForChange(result.get(0), result.get(1), f)
                )
        ).setHandler(resultHandler);
    }

    private void batchGetForChange(String source, String target, Handler<AsyncResult<List<AppItem>>> resultHandler) {
        var keys = new ArrayList<Map<String, AttributeValue>>(2);
        keys.add(Map.of(HASH_KEY_ID, AttributeValue.builder().s(source).build()));
        keys.add(Map.of(HASH_KEY_ID, AttributeValue.builder().s(source).build()));
        var request = BatchGetItemRequest.builder()
                .requestItems(Map.of(tableName, KeysAndAttributes.builder()
                        .keys(keys).build())).build();
        client.batchGetItem(request).whenComplete((response, err) ->
                pcall(() -> {
                    if (response != null) {
                        var items = response.responses().get(tableName);
                        if (Objects.isNull(items) || items.size() != 2) {
                            resultHandler.handle(Future.failedFuture("app not exist!"));
                        } else {
                            resultHandler.handle(Future.succeededFuture(froms(items)));
                        }

                    } else {
                        resultHandler.handle(Future.failedFuture(err));
                    }
                }));
    }

    private void batchWriteForChange(AppItem source, AppItem target, Handler<AsyncResult<Void>> resultHandler) {
        var ctime = source.ctime();
        source.ctime(target.ctime());
        target.ctime(ctime);

        List<WriteRequest> requests = new ArrayList<>(2);
        requests.add(WriteRequest.builder()
                .putRequest(PutRequest.builder().item(toItem(source)).build())
                .build());
        requests.add(WriteRequest.builder()
                .putRequest(PutRequest.builder().item(toItem(target)).build())
                .build());
        final var requestItems = Map.of(tableName, requests);

        final var batchWriteItemRequest = BatchWriteItemRequest.builder()
                .requestItems(requestItems)
                .build();

        client.batchWriteItem(batchWriteItemRequest).whenComplete((response, err) ->
                pcall(() -> {
                    if (response != null && response.unprocessedItems().size() == 0) {
                        resultHandler.handle(Future.succeededFuture());
                    } else {
                        resultHandler.handle(Future.failedFuture(err));
                    }
                }));
    }

    @Override
    public void count(Handler<AsyncResult<Long>> resultHandler) {
        var builder = DescribeTableRequest.builder().tableName(tableName);
        client.describeTable(builder.build()).whenComplete((response, err) ->
                pcall(() -> Optional.of(response)
                        .ifPresentOrElse(
                                resp -> Optional.of(resp.table())
                                        .filter(table -> Objects.nonNull(table.tableStatus())
                                                && table.tableStatus() == TableStatus.ACTIVE)
                                        .ifPresentOrElse(
                                                table -> resultHandler.handle(Future.succeededFuture(table.itemCount())),
                                                () -> resultHandler.handle(Future.succeededFuture(0L))),
                                () -> resultHandler.handle(Future.failedFuture(err))

                        )));
    }

    @Override
    public void createTable(Handler<AsyncResult<Void>> resultHandle) {

        var builder = CreateTableRequest.builder()
                .tableName(tableName)
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName(HASH_KEY_ID)
                                .keyType(KeyType.HASH)
                                .build())
                .attributeDefinitions(getAttributeDefinitions())
                .globalSecondaryIndexes(getGlobalSecondaryIndexes());
        DynamoDbCreateTable.builder()
                .client(client).config(config).vertx(vertx)
                .tableName(tableName)
                .build()
                .createTable(builder, resultHandle, false);

    }


    private Collection<AttributeDefinition> getAttributeDefinitions() {
        return List.of(AttributeDefinition.builder()
                        .attributeName(HASH_KEY_ID)
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                AttributeDefinition.builder()
                        .attributeName(ATTRIBUTE_CREATE_TIME)
                        .attributeType(ScalarAttributeType.N)
                        .build(),
                AttributeDefinition.builder()
                        .attributeName(GSI_HASH_KEY)
                        .attributeType(ScalarAttributeType.S)
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

    private static List<AppItem> froms(List<Map<String, AttributeValue>> items) {
        if (Objects.isNull(items) || items.isEmpty()) {
            return Collections.emptyList();
        }

        var list = new ArrayList<AppItem>(items.size());
        for (Map<String, AttributeValue> item : items) {
            list.add(from(item));
        }
        return list;
    }

    private static AppItem from(Map<String, AttributeValue> item) {
        if (Objects.isNull(item) || item.size() == 0) {
            return null;
        }
        return AppItem.builder()
                .app(item.get(HASH_KEY_ID).s())
                .ctime(Integer.parseInt(item.get(ATTRIBUTE_CREATE_TIME).n()))
                .description(item.get(ATTRIBUTE_DESCRIPTION).s())
                .alias(item.get(ATTRIBUTE_ALIAS).s())
                .build();
    }

    private static Map<String, AttributeValue> toItem(AppItem appItem) {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put(HASH_KEY_ID, AttributeValue.builder().s(appItem.app()).build());
        map.put(ATTRIBUTE_CREATE_TIME, AttributeValue.builder().n(Long.toString(appItem.ctime())).build());
        map.put(ATTRIBUTE_DESCRIPTION, AttributeValue.builder().s(appItem.description()).build());
        map.put(ATTRIBUTE_ALIAS, AttributeValue.builder().s(appItem.alias()).build());
        map.put(GSI_HASH_KEY, AttributeValue.builder().s(GSI_HASH_KEY).build());
        return map;
    }
}