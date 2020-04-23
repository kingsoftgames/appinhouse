package com.seasungames.model;

import lombok.Getter;

/**
 * Created by jianghaitao on 2019/8/3.
 */

@Getter
public enum ErrorCode {
    SUCCESS(0, "success"),
    ERROR(1, "request error"),
    DB_ERROR(2, "db error"),
    PARAM_ERROR(3, "params error"),
    DATA_ERROR(4, "mail data error");


    private int code;
    private String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
