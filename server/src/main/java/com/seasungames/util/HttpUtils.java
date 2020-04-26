package com.seasungames.util;

import com.seasungames.model.ErrorCode;
import com.seasungames.model.Request;
import com.seasungames.model.ResponseData;
import io.netty.util.internal.StringUtil;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import javax.validation.Validator;
import java.util.Objects;


/**
 * Created by wangzhiguang on 2019-04-03.
 */
@RegisterForReflection
public final class HttpUtils {

    public static <T> T parseRequest(RoutingContext rc, Class<T> clazz) {
        var body = getBody(rc);
        T ret = null;
        if (Objects.nonNull(body)) {
            try {
                ret = rc.getBodyAsJson().mapTo(clazz);
            } catch (IllegalArgumentException e) {
            }
        }
        return ret;
    }

    private static JsonObject getBody(RoutingContext rc) {
        try {
            return rc.getBodyAsJson();
        } catch (DecodeException e) {
            return null;
        }
    }

    public static <T> void validate(Validator validator, Request request, ResponseData<T> responseData) {
        if (Objects.nonNull(request)) {
            if (request.isInvalid(validator)) {
                responseData.setParamErrorMessage(request.getParamErrorMessage());
            }
        } else {
            responseData.setErrorCode(ErrorCode.PARAM_ERROR);
        }
    }

    public static int getIntWithDefault(HttpServerRequest request, String name, int defaultValue) {
        var value = request.getParam(name);
        if (StringUtil.isNullOrEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static long getLongWithDefault(HttpServerRequest request, String name, long defaultValue) {
        var value = request.getParam(name);
        if (StringUtil.isNullOrEmpty(value)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }
}
