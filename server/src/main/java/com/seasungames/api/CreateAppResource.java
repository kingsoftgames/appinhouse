package com.seasungames.api;

import com.seasungames.config.ApiConfig;
import com.seasungames.db.AppStore;
import com.seasungames.db.pojo.AppItem;
import com.seasungames.model.CreateAppRequest;
import com.seasungames.model.ErrorCode;
import com.seasungames.model.ResponseData;
import com.seasungames.model.ResponseDataUtil;
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
import java.time.Instant;

/**
 * Created by jianghaitao on 2020/4/22.
 */
public class CreateAppResource {

    private static final Logger log = LoggerFactory.getLogger(CreateAppResource.class);

    @Inject
    Validator validator;

    @Inject
    AppStore appStore;

    @Inject
    ApiConfig apiConfig;


    @Route(methods = HttpMethod.POST, path = "v2/app/create", type = HandlerType.NORMAL)
    void createApp(RoutingContext rc) {
        ResponseData<Void> responseData = ResponseDataUtil.buildSuccess();
        var request = HttpUtils.parseRequest(rc, CreateAppRequest.class);
        HttpUtils.validate(validator, request, responseData);
        if (responseData.error()) {
            rc.response().end(Json.encode(responseData));
            return;
        }

        var appItem = AppItem.builder()
                .ctime(Instant.now().getEpochSecond())
                .description(request.getDescription())
                .app(request.getApp())
                .alias(request.getAlias()).build();
        appStore.save(appItem, ar -> {
            if (ar.succeeded()) {
                if (!ar.result()) {
                    responseData.setErrorCode(ErrorCode.APP_EXIST);
                }
            } else {
                responseData.setErrorCode(ErrorCode.DB_ERROR, ar.cause());
            }
            rc.response().end(Json.encode(responseData));
        });
    }
}
