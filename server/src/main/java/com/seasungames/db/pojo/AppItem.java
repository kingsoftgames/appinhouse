package com.seasungames.db.pojo;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by jianghaitao on 2020/4/21.
 */

@Getter
@Setter
@Builder
@Accessors(fluent = true)
@RegisterForReflection
public class AppItem {
    private String app;
    private String description;
    private String alias;
    private long ctime;
    private int ttl;
}
