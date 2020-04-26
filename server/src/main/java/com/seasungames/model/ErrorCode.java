package com.seasungames.model;

import lombok.Getter;

/**
 * Created by jianghaitao on 2019/8/3.
 */

@Getter
public enum ErrorCode {
    // 1~99为公共错误
    SUCCESS(0, "success"),
    ERROR(1, "request error"),
    DB_ERROR(2, "db error"),
    PARAM_ERROR(3, "params error"),

    // 100~199为应用相关的错误
    APP_EXIST(100, "app exist!");


    private int code;
    private String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
