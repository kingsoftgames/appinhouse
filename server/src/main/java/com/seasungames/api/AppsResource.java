package com.seasungames.api;

import com.seasungames.config.ApiConfig;
import com.seasungames.db.AppStore;
import com.seasungames.db.pojo.AppItem;
import com.seasungames.model.*;
import com.seasungames.util.HttpUtils;
import io.netty.util.internal.StringUtil;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Route.HandlerType;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by jianghaitao on 2020/4/22.
 */
public class AppsResource {

    private static final Logger log = LoggerFactory.getLogger(AppsResource.class);

    @Inject
    Validator validator;

    @Inject
    AppStore appStore;

    @Inject
    ApiConfig apiConfig;

    private int pageSize;

    @PostConstruct
    void init(){
        pageSize = apiConfig.pageSize();
    }


    @Route(methods = HttpMethod.GET, path = "v2/apps", type = HandlerType.NORMAL)
    void getApps(RoutingContext rc) {
        ResponseData<AppsResponse> responseData = ResponseDataUtil.buildSuccess();
        var request = appsRequest(rc);
        validate(request, responseData);
        if (responseData.error()) {
            rc.response().end(Json.encode(responseData));
            return;
        }
        appStore.load(ar -> {
            if (ar.succeeded()) {
                toResponses(ar.result(), request.getPage(), responseData);
            } else {
                responseData.setErrorCode(ErrorCode.DB_ERROR);
            }
            rc.response().end(Json.encode(responseData));
        });
    }

    private void validate(AppsRequest request, ResponseData<AppsResponse> responseData) {
        Set<ConstraintViolation<AppsRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            responseData.setParamErrorMessage(violations);
        }
    }

    private AppsRequest appsRequest(RoutingContext rc) {
        var page = rc.request().getParam("page");
        if (StringUtil.isNullOrEmpty(page)) {
            page = "1";
        }
        return AppsRequest.builder().page(Integer.parseInt(page)).build();
    }

    private void toResponses(List<AppItem> list, int page, ResponseData<AppsResponse> responseData) {
        if (list == null || list.isEmpty()) {
            return;
        }
        var start = HttpUtils.getStart(page, pageSize);
        var end = HttpUtils.getEnd(page, pageSize);
        int size = list.size();
        end = Math.min(end, size);
        var totalPage = HttpUtils.getTotalPage(size, pageSize);
        list.sort((a, b) -> Long.compare(b.ctime(), a.ctime()));
        List<AppResponse> ret = new ArrayList<>(list.size());
        var sub = list.subList(start, end);
        for (var app : sub) {
            ret.add(AppResponse.from(app));
        }
        responseData.setData(AppsResponse.builder().items(ret).page(page).totalPage(totalPage).build());
    }
}
