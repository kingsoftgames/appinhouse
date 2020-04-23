package com.seasungames.db.dynamodb;

import com.seasungames.config.DbConfig;
import io.vertx.core.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.seasungames.util.FunctionUtils.pcall;

/**
 * Created by jianghaitao on 2020/4/20.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
public class DynamoDbCreateTable {
    private static final int WAIT_TABLE_AVAILABLE_MAX_CALL_COUNT = 30;
    private static final int WAIT_TABLE_AVAILABLE_CALL_SLEEP = 10 * 1000;
    private static final int FIRST = 1;
    private static final Logger log = LoggerFactory.getLogger(DynamoDbCreateTable.class);

    private DynamoDbAsyncClient client;
    private Vertx vertx;
    DbConfig config;
    private String ttlName;
    private String tableName;
    private boolean addTTL;

    public void createTableWithTTL(CreateTableRequest.Builder builder, Handler<AsyncResult<Void>> resultHandler) {
        if (config.onDemandBilling()) {
            builder.billingMode(BillingMode.PAY_PER_REQUEST);
        } else {
            builder.provisionedThroughput(ProvisionedThroughput.builder()
                    .readCapacityUnits(config.tableReadThroughput())
                    .writeCapacityUnits(config.tableWriteThroughput())
                    .build());
        }
        var request = builder.build();
        Future<Void> create = Future.future(f -> create(request, f));

        create.compose(v ->
                Future.future(this::isAvailable)
        ).compose(v -> {
            if (v) {
                return Future.succeededFuture();
            } else {
                var count = new AtomicInteger(FIRST);
                return Future.<Void>future(f -> waitForTableToBecomeAvailable(count, f));
            }
        }).compose(v -> {
            if (addTTL) {
                return Future.<Void>future(this::addTTL);
            } else {
                return Future.succeededFuture();
            }
        }).setHandler(resultHandler);
    }

    private void create(CreateTableRequest request, Handler<AsyncResult<Void>> resultHandler) {
        client.createTable(request).whenComplete((response, err) ->
                pcall(() -> {
                    if (err != null && !(err.getCause() instanceof ResourceInUseException)) {
                        resultHandler.handle(Future.failedFuture(err));
                    } else {
                        if (err != null && err.getCause() instanceof ResourceInUseException) {
                            log.info("DynamoDB table already creating or created : {}", tableName);
                        }
                        if (response != null) {
                            log.info("DynamoDB table created successfully: {}", tableName);
                            addTTL = true;
                        }
                        resultHandler.handle(Future.succeededFuture());
                    }
                }));
    }

    private void waitForTableToBecomeAvailable(AtomicInteger waitCount, Handler<AsyncResult<Void>> resultHandler) {
        log.info("Waiting for {} to become ACTIVE. ", tableName);
        vertx.setPeriodic(WAIT_TABLE_AVAILABLE_CALL_SLEEP, id -> {
            var count = waitCount.intValue();
            if (count > (WAIT_TABLE_AVAILABLE_MAX_CALL_COUNT)) {
                resultHandler.handle(Future.failedFuture(new RuntimeException("Table " + tableName + " never went active")));
                return;
            }
            Future<Boolean> available = Future.future(this::isAvailable);
            available.setHandler(ar -> {
                if (ar.succeeded()) {
                    if (ar.result()) {
                        vertx.cancelTimer(id);
                        resultHandler.handle(Future.succeededFuture());
                    } else {
                        log.info("Waiting for {} to become ACTIVE. describeTable {}th", tableName, count);
                        waitCount.getAndIncrement();
                    }
                } else {
                    resultHandler.handle(Future.failedFuture(ar.cause()));
                }
            });
        });
    }

    private void isAvailable(Handler<AsyncResult<Boolean>> resultHandler) {

        var builder = DescribeTableRequest.builder().tableName(tableName);
        client.describeTable(builder.build()).whenComplete((response, err) ->

                pcall(() -> {
                    if (response != null) {
                        var table = response.table();
                        if (Objects.isNull(table) || table.tableStatus() != TableStatus.ACTIVE) {
                            log.info("Waiting for {} to become ACTIVE. Cause table is null or not active", tableName);
                            resultHandler.handle(Future.succeededFuture(false));
                            return;
                        }
                        log.info("Waiting for {} to become ACTIVE. It is ACTIVE", tableName);
                        resultHandler.handle(Future.succeededFuture(true));
                    } else {
                        if (err.getCause() instanceof ResourceNotFoundException) {
                            log.info("Waiting for {} to become ACTIVE. Cause ResourceNotFoundException", tableName);
                            resultHandler.handle(Future.succeededFuture(false));
                            return;
                        }
                        resultHandler.handle(Future.failedFuture(err));
                    }
                }));
    }

    private void addTTL(Promise<Void> promise) {
        var ttlSpec = TimeToLiveSpecification.builder()
                .attributeName(ttlName)
                .enabled(true)
                .build();

        var ttlReq = UpdateTimeToLiveRequest.builder()
                .tableName(tableName)
                .timeToLiveSpecification(ttlSpec)
                .build();

        client.updateTimeToLive(ttlReq).whenComplete((response, err) ->
                pcall(() -> {
                    if (response != null) {
                        log.info("DynamoDB table Add TTL successfully: {}", tableName);
                        promise.complete();
                    } else {
                        log.info("DynamoDB table Add TTL failed: {}", tableName);
                        promise.fail(err);
                    }
                }));
    }
}
