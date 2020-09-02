package com.seasungames.api;

import com.seasungames.constant.Platform;
import com.seasungames.db.AppVersionStore;
import com.seasungames.db.pojo.AppVersionItem;
import com.seasungames.model.*;
import com.seasungames.util.HttpUtils;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Route.HandlerType;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.Validator;

/**
 * Created by jianghaitao on 2020/4/27.
 */
public class AppVersionResource {

    private static final Logger log = LoggerFactory.getLogger(AppVersionResource.class);

    @Inject
    Validator validator;

    @Inject
    AppVersionStore appVersionStore;

    @Route(methods = HttpMethod.GET, path = "v2/:app/app-version/:platform/:version", type = HandlerType.NORMAL)
    void getAppVersion(RoutingContext rc) {
        ResponseData<AppVersionResponse> responseData = ResponseDataUtil.buildSuccess();
        var request = appsRequest(rc);
        HttpUtils.validate(validator, request, responseData);
        if (responseData.error()) {
            rc.response().end(Json.encode(responseData));
            return;
        }
        appVersionStore.get(AppVersionItem.generateId(Platform.of(request.getPlatform()), request.getApp()), request.getVersion(), ar -> {
            if (ar.succeeded()) {
                var ret = ar.result();
                ret.ifPresent(item -> responseData.setData(AppVersionResponse.from(item)));
            } else {
                responseData.setErrorCode(ErrorCode.DB_ERROR, ar.cause());
            }
            rc.response().end(Json.encode(responseData));
        });
    }


    private static AppVersionRequest appsRequest(RoutingContext rc) {
        var app = rc.request().getParam("app");
        var version = rc.request().getParam("version");
        var platform = rc.request().getParam("platform");
        return AppVersionRequest.builder().platform(platform).app(app).version(version).build();
    }
}
