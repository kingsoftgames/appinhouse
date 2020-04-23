package com.seasungames.model;

import com.seasungames.db.pojo.AppItem;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by jianghaitao on 2020/4/22.
 */
@Getter
@Setter
@Builder
@RegisterForReflection
public class AppResponse {
    private String app;

    private String description;

    private String alias;

    public static AppResponse from(AppItem appItem) {
        return AppResponse.builder()
                .alias(appItem.alias())
                .app(appItem.app())
                .description(appItem.description())
                .build();
    }
}
