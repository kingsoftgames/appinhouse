package com.seasungames.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.netty.util.internal.StringUtil;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by jianghaitao on 2019/8/3.
 */
@Getter
@Setter
@Builder
@RegisterForReflection
public class ResponseData<T> {
    private int code;

    private String msg;

    @JsonInclude(Include.NON_NULL)
    private T data;

    public ResponseData(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResponseData(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResponseData(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMsg());
    }

    public ResponseData(ErrorCode errorCode, T data) {
        this(errorCode.getCode(), errorCode.getMsg(), data);
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.msg = errorCode.getMsg();
    }

    public void setErrorCode(ErrorCode errorCode, String msg) {
        this.code = errorCode.getCode();
        this.msg = msg;
    }

    public void setErrorCode(ErrorCode errorCode, Throwable throwable) {
        this.code = errorCode.getCode();
        var msg = throwable.getMessage();
        if (!StringUtil.isNullOrEmpty(msg)) {
            this.msg = msg;
        }
    }

    public void setParamErrorMessage(String errorMessage) {
        this.code = ErrorCode.PARAM_ERROR.getCode();
        this.msg = errorMessage;
    }

    public boolean error() {
        return this.code != ErrorCode.SUCCESS.getCode();
    }

    public ResponseData() {
    }

}
