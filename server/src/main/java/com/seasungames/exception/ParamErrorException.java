package com.seasungames.exception;

/**
 * Created by jianghaitao on 2020/4/27.
 */
public class ParamErrorException extends Throwable {
    public ParamErrorException(String message) {
        super(message, null, false, false);
    }
}
