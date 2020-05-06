package com.seasungames.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by jianghaitao on 2020/4/21.
 */
public enum Environment {

    RELEASE("release"),
    DEV("dev");

    private static final Map<String, Environment> map;

    static {
        map = Arrays.stream(Environment.values())
                .collect(Collectors.toMap(Environment::getName, e -> e));
    }

    private String name;

    Environment(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Optional<Environment> of(String operation) {
        return Optional.ofNullable(map.get(operation));
    }

}
