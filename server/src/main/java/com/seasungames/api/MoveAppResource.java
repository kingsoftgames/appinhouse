package com.seasungames.api;

import com.seasungames.constant.MoveOperation;
import com.seasungames.db.AppStore;
import com.seasungames.db.pojo.AppItem;
import com.seasungames.exception.ParamErrorException;
import com.seasungames.model.ErrorCode;
import com.seasungames.model.MoveAppRequest;
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
import java.util.Optional;

/**
 * Created by jianghaitao on 2020/4/22.
 */
public class MoveAppResource {

    private static final Logger log = LoggerFactory.getLogger(MoveAppResource.class);

    @Inject
    Validator validator;

    @Inject
    AppStore appStore;

    @Route(methods = HttpMethod.POST, path = "v2/app/move", type = HandlerType.NORMAL)
    void createApp(RoutingContext rc) {
        ResponseData<Void> responseData = ResponseDataUtil.buildSuccess();
        var request = HttpUtils.parseRequest(rc, MoveAppRequest.class);
        HttpUtils.validate(validator, request, responseData);
        if (responseData.error()) {
            rc.response().end(Json.encode(responseData));
            return;
        }
        process(request, ar -> {
            if (ar.failed()) {
                if (ar.cause() instanceof ParamErrorException) {
                    responseData.setErrorCode(ErrorCode.PARAM_ERROR, ar.cause());
                } else {
                    responseData.setErrorCode(ErrorCode.DB_ERROR, ar.cause());
                }
            }
            rc.response().end(Json.encode(responseData));
        });
    }

    private void process(MoveAppRequest request, Handler<AsyncResult<Void>> resultHandler) {
        Future<Optional<AppItem>> getSource = Future.future(f -> appStore.get(request.getApp(), f));
        var ref = new Object() {
            AppItem source;
        };
        getSource.compose(sourceOptional ->
                Future.<Optional<AppItem>>future(f ->
                        sourceOptional.ifPresentOrElse(
                                source -> {
                                    ref.source = source;
                                    getTarget(source, request.getOperation(), f);
                                },
                                () -> f.handle(Future.failedFuture(new ParamErrorException("source is not exist")))))
        ).compose(targetOptional ->
                Future.<Void>future(f ->
                        targetOptional.ifPresentOrElse(
                                target -> {
                                    if (ref.source.app().equals(target.app())) {
                                        f.handle(Future.failedFuture(new ParamErrorException("source eq target")));
                                    } else {
                                        appStore.change(ref.source, target, f);
                                    }
                                },
                                () -> f.handle(Future.failedFuture(new ParamErrorException("target is not exist")))))
        ).setHandler(resultHandler);
    }

    private void getTarget(AppItem appItem, String operation, Handler<AsyncResult<Optional<AppItem>>> resultHandler) {
        switch (MoveOperation.of(operation).get()) {
            case UP:
                appStore.getNext(appItem.ctime(), true, resultHandler);
                break;
            case DOWN:
                appStore.getNext(appItem.ctime(), false, resultHandler);
                break;
            case TOP:
                appStore.getFirst(false, resultHandler);
                break;
            case END:
                appStore.getFirst(true, resultHandler);
                break;
            default:
                resultHandler.handle(Future.succeededFuture(Optional.empty()));
        }
    }
}
