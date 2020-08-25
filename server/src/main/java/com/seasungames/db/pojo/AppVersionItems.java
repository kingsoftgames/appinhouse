package com.seasungames.db.pojo;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Created by jianghaitao on 2020/4/28.
 */

@Getter
@Setter
@Builder
@Accessors(fluent = true)
@RegisterForReflection
public class AppVersionItems {
    private List<AppVersionItem> items;
    private boolean hasMore;
}
