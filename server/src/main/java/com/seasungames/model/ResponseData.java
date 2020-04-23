package com.seasungames.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Collectors;

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
        this.code = errorCode.getCode();
        this.msg = errorCode.getMsg();
    }

    public ResponseData(ErrorCode errorCode, T data) {
        this.code = errorCode.getCode();
        this.msg = errorCode.getMsg();
        this.data = data;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.msg = errorCode.getMsg();
    }

    public void setParamErrorMessage(Set<ConstraintViolation<AppsRequest>> violations) {
        this.code = ErrorCode.PARAM_ERROR.getCode();
        this.msg = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
    }

    public boolean error() {
        return this.code != ErrorCode.SUCCESS.getCode();
    }

    public ResponseData() {
    }

}
