package com.seasungames.util;

import com.seasungames.model.AppsRequest;
import com.seasungames.model.AppsResponse;
import com.seasungames.model.ResponseData;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.RoutingContext;

import javax.validation.ConstraintViolation;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Map;
import java.util.Set;


/**
 * Created by wangzhiguang on 2019-04-03.
 */
public final class HttpUtils {

    private static final Decoder base64Decoder = Base64.getDecoder();

    private static final Encoder base64Encoder = Base64.getEncoder();

    public static <T> T parseRequest(RoutingContext rc, Class<T> clazz) {
        try {
            return rc.getBodyAsJson().mapTo(clazz);
        } catch (DecodeException | IllegalArgumentException e) {
            return null;
        }
    }

    public static <T> T parseRequestFromMap(Map<String, String> map, Class<T> clazz) {
        try {
            return DatabindCodec.mapper().convertValue(map, clazz);
        } catch (DecodeException | IllegalArgumentException e) {
            return null;
        }
    }

    public static int getStart(int page, int pageSize) {
        if (page == 1) {
            return 0;
        }
        return ((page - 1) * pageSize);
    }

    public static int getEnd(int page, int pageSize) {
        return (page * pageSize) - 1;
    }

    public static int getTotalPage(int total, int pageSize) {
        if (total <= pageSize) {
            return 1;
        }
        var ret = (total / pageSize);
        if ((total % pageSize) > 0) {
            ret = ret + 1;
        }
        return ret;
    }
}
