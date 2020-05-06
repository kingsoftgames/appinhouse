package com.seasungames.api;

import com.seasungames.db.AppStore;
import com.seasungames.model.AddDescRequest;
import com.seasungames.model.ResponseData;
import com.seasungames.model.ResponseDataUtil;
import com.seasungames.util.HttpUtils;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Route.HandlerType;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.Validator;

/**
 * Created by jianghaitao on 2020/4/27.
 */
public class AddDescResource {

    private static final Logger log = LoggerFactory.getLogger(AddDescResource.class);

    @Inject
    Validator validator;

    @Inject
    AppStore appStore;

    @Route(methods = HttpMethod.POST, path = "v2/desc/add", type = HandlerType.NORMAL)
    void add(RoutingContext rc) {
        ResponseData<Void> responseData = ResponseDataUtil.buildSuccess();
        var request = HttpUtils.parseRequest(rc, AddDescRequest.class);
        HttpUtils.validate(validator, request, responseData);
        if (responseData.error()) {
            rc.response().end(Json.encode(responseData));
            return;
        }
        rc.response().end(Json.encode(responseData));
    }
}
