package com.seasungames.db;

import com.seasungames.db.pojo.AppItem;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;
import java.util.Optional;

/**
 * Created by jianghaitao on 2020/4/20.
 */
public interface AppStore {
    void createTable(Handler<AsyncResult<Void>> resultHandler);

    void save(AppItem appItem, Handler<AsyncResult<Boolean>> resultHandler);

    void getFirst(boolean ascend, Handler<AsyncResult<Optional<AppItem>>> resultHandler);

    void getByLimit(long ctime, int limit, Handler<AsyncResult<List<AppItem>>> resultHandler);

    void get(String app, Handler<AsyncResult<Optional<AppItem>>> resultHandler);

    void delete(String app, Handler<AsyncResult<Void>> resultHandler);

    void change(AppItem source, AppItem target, Handler<AsyncResult<Void>> resultHandler);

    void getNext(long ctime, boolean ascend, Handler<AsyncResult<Optional<AppItem>>> resultHandler);
}
