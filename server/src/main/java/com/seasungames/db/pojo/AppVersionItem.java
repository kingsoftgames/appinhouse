package com.seasungames.db.pojo;

import com.seasungames.constant.Platform;
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
public class AppVersionItem {
    private String id;
    private String version;
    private String url;
    private long time;
    private String desc;
    private long ttl;

    public static String generateId(Platform platform, String app) {
        return platform.name() + ":" + app;
    }


    public String app() {
        return id.split(":")[1];
    }

    public String platform() {
        return id.split(":")[0];
    }
}
