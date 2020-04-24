package com.seasungames.filter;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by jianghaitao on 2020/4/24.
 */
public interface Filter {

    default void next(RoutingContext rc, boolean pass, int code, String message) {
        if (pass) {
            rc.next();
        } else {
            rc.response().setStatusCode(code).end(message);
        }
    }

    default void handle(RoutingContext rc, int code, String message) {
        var request = rc.request();
        var pass = true;
        if (nonGetMethod(request)) {
            if (isInvalidRequest(request)) {
                pass = false;
            }
        }
        next(rc, pass, code, message);
    }

    boolean isInvalidRequest(HttpServerRequest request);

    default boolean nonGetMethod(HttpServerRequest request) {
        return request.method() != HttpMethod.GET;
    }
}
