package com.seasungames.db.pojo;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
}
