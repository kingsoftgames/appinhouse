package com.seasungames.filter;

import com.seasungames.config.ApiConfig;
import io.netty.util.internal.StringUtil;
import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by jianghaitao on 2020/4/22.
 */
public class SecretKeyFilter implements Filter {

    private static int code = 401;
    private static String message = "Missing or invalid X-SecretKey HTTP header";
    private static final String validName = "X-SecretKey";

    @Inject
    ApiConfig apiConfig;

    private String secretKey;

    @PostConstruct
    void init() {
        secretKey = apiConfig.secretKey();
    }

    // 100 是filter的执行顺序 ,越大越先执行
    @RouteFilter(198)
    void filter(RoutingContext rc) {
        handle(rc, code, message);
    }

    @Override
    public boolean isInvalidRequest(HttpServerRequest request) {
        var key = request.headers().get(validName);
        return StringUtil.isNullOrEmpty(key) || !key.equals(secretKey);
    }
}
