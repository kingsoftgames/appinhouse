package com.seasungames.filter;

import com.seasungames.config.ApiConfig;
import io.netty.util.internal.StringUtil;
import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by jianghaitao on 2020/4/22.
 */
public class Filters {

    @Inject
    ApiConfig apiConfig;

    private String secretKey;

    @PostConstruct
    void init() {
        secretKey = apiConfig.secretKey();
    }

    // 100 是filter的执行顺序
    @RouteFilter(100)
    void myFilter(RoutingContext rc) {
        var request = rc.request();
        var pass = true;
        if (nonGetMethod(request)) {
            if (isInvalidSecretKey(request, secretKey)) {
                pass = false;
            }
        }
        next(rc, pass);
    }

    private static void next(RoutingContext rc, boolean pass) {
        if (pass) {
            rc.next();
        } else {
            rc.response().setStatusCode(400).end("Missing or invalid X-SecretKey HTTP header");
        }
    }

    private static boolean isInvalidSecretKey(HttpServerRequest request, String secretKey) {
        var key = request.headers().get("X-SecretKey");
        return StringUtil.isNullOrEmpty(secretKey) || !key.equals(secretKey);
    }

    private static boolean nonGetMethod(HttpServerRequest request) {
        return request.method() != HttpMethod.GET;
    }
}
