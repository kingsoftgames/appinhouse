package com.seasungames.filter;

import com.seasungames.config.ApiConfig;
import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by jianghaitao on 2020/4/24.
 */
public class ResponseContentTypeFilter {

    private static final String headerName = "Content-Type";

    @Inject
    ApiConfig apiConfig;

    private String contentType;

    @PostConstruct
    void init() {
        contentType = apiConfig.responseContentType();
    }

    @RouteFilter(197)
    void filter(RoutingContext rc) {
        rc.response().putHeader(headerName, contentType);
        rc.next();
    }
}
