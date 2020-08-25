package com.seasungames.db;

import com.seasungames.db.pojo.AppVersionItem;
import com.seasungames.db.pojo.AppVersionItems;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.Optional;

/**
 * Created by jianghaitao on 2020/4/20.
 */
public interface AppVersionStore {
    void createTable(Handler<AsyncResult<Void>> resultHandler);

    void save(AppVersionItem appVersionItem, Handler<AsyncResult<Void>> resultHandler);

    void get(String id, String version, Handler<AsyncResult<Optional<AppVersionItem>>> resultHandler);

    void getByLimit(String id, long ctime, int limit, Handler<AsyncResult<AppVersionItems>> resultHandler);

    void getLast(String id, Handler<AsyncResult<Optional<AppVersionItem>>> resultHandler);
}
