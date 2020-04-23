package com.seasungames.db;

import com.seasungames.db.pojo.DescItem;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;

/**
 * Created by jianghaitao on 2020/4/20.
 */
public interface DescStore {
    void createTable(Handler<AsyncResult<Void>> resultHandler);

    void save(DescItem descItem, Handler<AsyncResult<Void>> resultHandler);

    void get(String id, String version, Handler<AsyncResult<DescItem>> resultHandler);

    void getByLimit(String id, int limit, String startRangeKey, Handler<AsyncResult<List<DescItem>>> resultHandler);

    void getLast(String id, Handler<AsyncResult<DescItem>> resultHandler);
}
