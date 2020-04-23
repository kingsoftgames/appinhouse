package com.seasungames.db;

import com.seasungames.db.pojo.PlistItem;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * Created by jianghaitao on 2020/4/20.
 */
public interface PlistStore {
    void createTable(Handler<AsyncResult<Void>> resultHandler);

    void save(PlistItem plistItem, Handler<AsyncResult<Void>> resultHandler);

    void get(String id, String version, Handler<AsyncResult<String>> resultHandler);
}
