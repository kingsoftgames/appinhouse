package com.seasungames.db.pojo;

import com.seasungames.constant.Environment;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * Created by jianghaitao on 2020/4/21.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@RegisterForReflection
public class PlistItem {
    private String id;
    private String version;
    private String body;
    private long ttl;

    public static String generateId(Environment env, String app) {
        return env.name() + ":" + app;
    }

    public static String generateVersion(String version, String extendSoftwareUrlName) {
        return Objects.isNull(extendSoftwareUrlName) ? version : version + ":" + extendSoftwareUrlName;
    }
}
