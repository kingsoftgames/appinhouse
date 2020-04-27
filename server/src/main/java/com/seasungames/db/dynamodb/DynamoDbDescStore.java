package com.seasungames.db.dynamodb;

import com.seasungames.config.DbConfig;
import com.seasungames.db.DescStore;
import com.seasungames.db.pojo.DescItem;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

/**
 * Created by jianghaitao on 2020/4/27.
 */
@ApplicationScoped
public class DynamoDbDescStore implements DescStore {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbDescStore.class);
    private static final String HASH_KEY_ID = "id";
    private static final String RANGE_KEY_ID = "version";
    private static final String ATTRIBUTE_DESC = "desc";
    private static final String TTL = "ttl";

    @Inject
    Vertx vertx;

    @Inject
    DbConfig config;

    @Inject
    DynamoDbAsyncClient client;

    private String tableName;

    @PostConstruct
    void init() {
        tableName = config.descsTableName();
    }

    @Override
    public void createTable(Handler<AsyncResult<Void>> resultHandle) {

        var builder = CreateTableRequest.builder()
                .tableName(tableName)
                .keySchema(getKeySchema())
                .attributeDefinitions(getAttributeDefinitions());
        DynamoDbCreateTable.builder()
                .client(client).config(config).vertx(vertx)
                .tableName(tableName).ttlName(TTL)
                .build()
                .createTable(builder, resultHandle, true);

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
                        .build());
    }

    @Override
    public void save(DescItem descItem, Handler<AsyncResult<Void>> resultHandler) {

    }

    @Override
    public void get(String id, String version, Handler<AsyncResult<DescItem>> resultHandler) {

    }

    @Override
    public void getByLimit(String id, int limit, String startRangeKey, Handler<AsyncResult<List<DescItem>>> resultHandler) {

    }

    @Override
    public void getLast(String id, Handler<AsyncResult<DescItem>> resultHandler) {

    }
}
