package com.seasungames.api;

import com.seasungames.db.AppStore;
import com.seasungames.model.*;
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
 * Created by jianghaitao on 2020/4/22.
 */
public class AppResource {

    private static final Logger log = LoggerFactory.getLogger(AppResource.class);

    @Inject
    Validator validator;

    @Inject
    AppStore appStore;


    @Route(methods = HttpMethod.GET, path = "v2/app/:app", type = HandlerType.NORMAL)
    void getApp(RoutingContext rc) {
        ResponseData<AppResponse> responseData = ResponseDataUtil.buildSuccess();
        var request = appsRequest(rc);
        if (request.isInvalid(validator)) {
            responseData.setParamErrorMessage(request.getParamErrorMessage());
            rc.response().end(Json.encode(responseData));
            return;
        }
        appStore.get(request.getApp(), ar -> {
            if (ar.succeeded()) {
                ar.result().ifPresent(item -> responseData.setData(AppResponse.from(item)));
            } else {
                responseData.setErrorCode(ErrorCode.DB_ERROR);
            }
            rc.response().end(Json.encode(responseData));
        });
    }

    private AppRequest appsRequest(RoutingContext rc) {
        var app = rc.request().getParam("app");
        return AppRequest.builder().app(app).build();
    }
}
