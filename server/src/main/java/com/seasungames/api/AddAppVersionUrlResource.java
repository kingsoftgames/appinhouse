package com.seasungames.api;

import com.seasungames.constant.Platform;
import com.seasungames.db.AppVersionStore;
import com.seasungames.db.pojo.AppVersionItem;
import com.seasungames.model.AddAppVersionUrlRequest;
import com.seasungames.model.ErrorCode;
import com.seasungames.model.ResponseData;
import com.seasungames.model.ResponseDataUtil;
import com.seasungames.util.HttpUtils;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Route.HandlerType;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.Validator;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by jianghaitao on 2020/4/27.
 */
public class AddAppVersionUrlResource {

    private static final Logger log = LoggerFactory.getLogger(AddAppVersionUrlResource.class);

    @Inject
    Validator validator;

    @Inject
    AppVersionStore appVersionStore;

    @Route(methods = HttpMethod.POST, path = "v2/app-version/url/add", type = HandlerType.NORMAL)
    void add(RoutingContext rc) {
        ResponseData<Void> responseData = ResponseDataUtil.buildSuccess();
        var request = HttpUtils.parseRequest(rc, AddAppVersionUrlRequest.class);
        HttpUtils.validate(validator, request, responseData);
        if (responseData.error()) {
            rc.response().end(Json.encode(responseData));
            return;
        }
        save(request, ar -> {
            if (ar.failed()) {
                responseData.setErrorCode(ErrorCode.DB_ERROR);
            } else {
                responseData.setErrorCode(ar.result());
            }
            rc.response().end(Json.encode(responseData));
        });

    }

    private void save(AddAppVersionUrlRequest request, Handler<AsyncResult<ErrorCode>> resultHandler) {
        AppVersionItem item = toItem(request);
        appVersionStore.get(item.id(), item.version(), ar -> {
            if (ar.succeeded()) {
                if (ar.result().isPresent()) {
                    var moreUrls = ar.result().get().moreUrls();
                    if (Objects.isNull(moreUrls)) {
                        moreUrls = new HashMap<>();
                    }
                    moreUrls.put(request.getName(), request.getUrl());
                    item.moreUrls(moreUrls);
                    addAppVersionUrl(item, resultHandler);
                } else {
                    resultHandler.handle(Future.succeededFuture(ErrorCode.APP_VERSION_NOT_EXIST));

                }
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    private void addAppVersionUrl(AppVersionItem item, Handler<AsyncResult<ErrorCode>> resultHandler) {
        appVersionStore.addUrl(item, ar -> {
            if (ar.failed()) {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            } else {
                resultHandler.handle(Future.succeededFuture(ErrorCode.SUCCESS));
            }
        });
    }

    private AppVersionItem toItem(AddAppVersionUrlRequest request) {
        String url = request.getUrl();
        Platform platform = getInstallPack(url);
        return AppVersionItem.builder()
                .id(AppVersionItem.generateId(platform, request.getApp()))
                .version(request.getVersion())
                .build();
    }

    private static Platform getInstallPack(String url) {
        return url.endsWith("apk") ? Platform.ANDROID : Platform.IOS;
    }
}
