package com.seasungames.api;

import com.seasungames.constant.Platform;
import com.seasungames.db.AppStore;
import com.seasungames.db.AppVersionStore;
import com.seasungames.db.pojo.AppVersionItem;
import com.seasungames.model.AddAppVersionRequest;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;

/**
 * Created by jianghaitao on 2020/4/27.
 */
public class AddAppVersionResource {

    private static final Logger log = LoggerFactory.getLogger(AddAppVersionResource.class);

    @Inject
    Validator validator;

    @Inject
    AppStore appStore;

    @Inject
    AppVersionStore appVersionStore;

    private final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @Route(methods = HttpMethod.POST, path = "v2/app-version/add", type = HandlerType.NORMAL)
    void add(RoutingContext rc) {
        ResponseData<Void> responseData = ResponseDataUtil.buildSuccess();
        var request = HttpUtils.parseRequest(rc, AddAppVersionRequest.class);
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

    private void save(AddAppVersionRequest request, Handler<AsyncResult<ErrorCode>> resultHandler) {
        appStore.exist(request.getApp(), ar -> {
            if (ar.succeeded()) {
                if (ar.result()) {
                    saveAddAppVersion(toItem(request), resultHandler);
                } else {
                    resultHandler.handle(Future.succeededFuture(ErrorCode.APP_NOT_EXIST));

                }
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    private void saveAddAppVersion(AppVersionItem item, Handler<AsyncResult<ErrorCode>> resultHandler) {
        appVersionStore.save(item, ar -> {
            if (ar.failed()) {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            } else {
                resultHandler.handle(Future.succeededFuture(ErrorCode.SUCCESS));
            }
        });
    }

    private AppVersionItem toItem(AddAppVersionRequest request) {
        String url = request.getUrl();
        Platform platform = getInstallPack(url);
        return AppVersionItem.builder()
                .id(AppVersionItem.generateId(platform, request.getApp()))
                .version(request.getVersion())
                .desc(request.getDescription())
                .time(iosDateToSecond(request.getTime()))
                .url(request.getUrl())
                .ttl(Instant.now().getEpochSecond())
                .build();
    }

    private static Platform getInstallPack(String url) {
        return url.endsWith("apk") ? Platform.ANDROID : Platform.IOS;
    }

    private long iosDateToSecond(String isoDate) {
        try {
            return isoFormat.parse(isoDate).toInstant().getEpochSecond();
        } catch (ParseException e) {
            return Instant.now().getEpochSecond();
        }
    }
}
