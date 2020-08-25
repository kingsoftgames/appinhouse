package com.seasungames.api;

import com.seasungames.db.AppStore;
import com.seasungames.db.AppVersionStore;
import com.seasungames.model.ErrorCode;
import com.seasungames.model.ResponseData;
import com.seasungames.model.ResponseDataUtil;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Route.HandlerType;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import javax.inject.Inject;

public class InitDbResource {

    @Inject
    AppStore appStore;

    @Inject
    AppVersionStore appVersionStore;

    @Route(methods = HttpMethod.POST, path = "v2/initDb", type = HandlerType.NORMAL)
    void initDb(RoutingContext rc) {
        ResponseData<Void> responseData = ResponseDataUtil.buildSuccess();
        
        Future.<Void>future(
                f -> appStore.createTable(f)
        ).compose(
                v -> Future.<Void>future(f -> appVersionStore.createTable(f))
        ).setHandler(ar -> {
            if (ar.failed()) {
                responseData.setErrorCode(ErrorCode.DB_ERROR);
            }
            rc.response().end(Json.encode(responseData));
        });
    }
}