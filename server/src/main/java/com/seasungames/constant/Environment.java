package com.seasungames.constant;

/**
 * Created by jianghaitao on 2020/4/21.
 */
public enum Environment {

    RELEASE("release"),
    DEV("dev");

    private String name;

    Environment(String name) {
        this.name = name;
    }
}
