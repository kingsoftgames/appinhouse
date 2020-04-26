package com.seasungames.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Created by jianghaitao on 2020/4/22.
 */
@RegisterForReflection
public class ResponseDataUtil {
    public static <T> ResponseData<T> buildSuccess(T data) {
        return new ResponseData<>(ErrorCode.SUCCESS, data);
    }

    public static <T> ResponseData<T> buildSuccess() {
        return new ResponseData<>(ErrorCode.SUCCESS);
    }

    public static <T> ResponseData<T> buildError(ErrorCode errorCode) {
        return new ResponseData<>(errorCode);
    }

    public static <T> ResponseData<T> buildError(ErrorCode errorCode, String msg) {
        return new ResponseData<>(errorCode.getCode(), msg);
    }
}
