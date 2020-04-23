package com.seasungames.db;

import com.seasungames.db.pojo.AppItem;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;

/**
 * Created by jianghaitao on 2020/4/20.
 */
public interface AppStore {
    void createTable(Handler<AsyncResult<Void>> resultHandler);

    void save(AppItem appItem, Handler<AsyncResult<Void>> resultHandler);

    void exist(String app, Handler<AsyncResult<Boolean>> resultHandler);

    void load(Handler<AsyncResult<List<AppItem>>> resultHandler);

    void get(String app, Handler<AsyncResult<AppItem>> resultHandler);

    void delete(String app, Handler<AsyncResult<Void>> resultHandler);

    void change(String source, String target, Handler<AsyncResult<Void>> resultHandler);

    void count(Handler<AsyncResult<Long>> resultHandler);
}
