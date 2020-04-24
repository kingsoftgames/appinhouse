package com.seasungames.api;

import com.seasungames.config.ApiConfig;
import com.seasungames.db.AppStore;
import com.seasungames.db.pojo.AppItem;
import com.seasungames.model.*;
import com.seasungames.util.HttpUtils;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Route.HandlerType;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.validation.Validator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
    void init() {
        pageSize = apiConfig.pageSize();
    }


    @Route(methods = HttpMethod.GET, path = "v2/apps", type = HandlerType.NORMAL)
    void getApps(RoutingContext rc) {
        ResponseData<AppsResponse> responseData = ResponseDataUtil.buildSuccess();
        var request = appsRequest(rc);
        if (request.isInvalid(validator)) {
            responseData.setParamErrorMessage(request.getParamErrorMessage());
            rc.response().end(Json.encode(responseData));
            return;
        }
        appStore.getByLimit(request.getLastCtime(), request.getLimit(), ar -> {
            if (ar.succeeded()) {
                toResponses(ar.result(), request, responseData);
            } else {
                responseData.setErrorCode(ErrorCode.DB_ERROR);
            }
            rc.response().end(Json.encode(responseData));
        });
    }


    private AppsRequest appsRequest(RoutingContext rc) {
        var request = rc.request();
        var limit = HttpUtils.getIntWithDefault(request, "limit", pageSize);
        var lastCtime = HttpUtils.getLongWithDefault(request, "ctime", Instant.now().getEpochSecond());
        var builder = AppsRequest.builder();
        return builder.limit(limit).lastCtime(lastCtime).build();
    }

    private void toResponses(List<AppItem> list, AppsRequest request, ResponseData<AppsResponse> responseData) {
        if (list.isEmpty()) {
            return;
        }

        List<AppResponse> ret = new ArrayList<>(list.size());
        for (var item : list) {
            ret.add(AppResponse.from(item));
        }
        responseData.setData(AppsResponse.builder().items(ret).build());
    }

//    private static int indexOfCtime(List<AppItem> list, long ctime) {
//        var ret = -1;
//        var size = list.size();
//        var maxCtime = list.get(0).ctime();
//        var minCtime = list.get(size-1).ctime();
//        if (ctime >= maxCtime){
//            ret = 0;
//            return ret;
//        }
//        if (ctime <= minCtime){
//            return ret;
//        }
//        for (int i = 0; i < size; i++) {
//            if (list.get(i).ctime() == ctime) {
//                ret = i + 1;
//                break;
//            }
//        }
//        return ret;
//    }
}
