package com.seasungames.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by jianghaitao on 2020/4/26.
 */

public enum MoveOperation {
    UP("up"),
    DOWN("down"),
    TOP("top"),
    END("end");

    private static final Map<String, MoveOperation> map;

    static {
        map = Arrays.stream(MoveOperation.values())
                .collect(Collectors.toMap(MoveOperation::getOperation, e -> e));
    }

    private String operation;

    MoveOperation(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

    public static Optional<MoveOperation> of(String operation) {
        return Optional.ofNullable(map.get(operation));
    }
}
