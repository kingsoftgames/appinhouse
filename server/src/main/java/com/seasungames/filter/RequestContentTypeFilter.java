package com.seasungames.filter;

import com.seasungames.config.ApiConfig;
import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Objects;

/**
 * Created by jianghaitao on 2020/4/24.
 */
public class RequestContentTypeFilter implements Filter {

    private static final int code = 406;
    private static final String message = "Content-Type is error";
    private static final String validName = "Content-Type";

    @Inject
    ApiConfig apiConfig;

    private String contentType;

    @PostConstruct
    void init() {
        contentType = apiConfig.requestContentType();
    }

    @RouteFilter(199)
    void filter(RoutingContext rc) {
        handle(rc, code, message);
    }

    @Override
    public boolean isInvalidRequest(HttpServerRequest request) {
        var type = request.getHeader(validName);
        return Objects.isNull(type) || !type.equals(contentType);
    }
}
