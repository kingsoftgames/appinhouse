package com.seasungames.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by jianghaitao on 2020/4/21.
 */
public enum Platform {
    ANDROID("android"),
    IOS(Constants.IOS_PLATFORM);

    private static final Map<String, Platform> map;

    static {
        map = Arrays.stream(Platform.values())
                .collect(Collectors.toMap(Platform::getName, e -> e));
    }

    private String name;

    Platform(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Optional<Platform> of(String operation) {
        return Optional.ofNullable(map.get(operation));
    }
}
