package com.seasungames.api;

import com.seasungames.db.AppStore;
import com.seasungames.model.DeleteAppRequest;
import com.seasungames.model.ErrorCode;
import com.seasungames.model.ResponseData;
import com.seasungames.model.ResponseDataUtil;
import com.seasungames.util.HttpUtils;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Route.HandlerType;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import javax.inject.Inject;
import javax.validation.Validator;

/**
 * Created by jianghaitao on 2020/4/26.
 */
public class DeleteAppResource {

    @Inject
    Validator validator;

    @Inject
    AppStore appStore;

    @Route(methods = HttpMethod.POST, path = "v2/app/delete", type = HandlerType.NORMAL)
    void deleteApp(RoutingContext rc) {
        ResponseData<Void> responseData = ResponseDataUtil.buildSuccess();
        var request = HttpUtils.parseRequest(rc, DeleteAppRequest.class);
        HttpUtils.validate(validator, request, responseData);
        if (responseData.error()) {
            rc.response().end(Json.encode(responseData));
            return;
        }

        appStore.delete(request.getApp(), ar -> {
            if (!ar.succeeded()) {
                responseData.setErrorCode(ErrorCode.DB_ERROR, ar.cause());
            }
            rc.response().end(Json.encode(responseData));
        });
    }
}
