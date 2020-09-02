package com.seasungames.model;

import com.seasungames.constant.Platform;
import com.seasungames.validation.Enum;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * Created by jianghaitao on 2020/4/27.
 */

@Getter
@Setter
@Builder
@RegisterForReflection
public class AppVersionRequest extends Request {

    @NotBlank(message = "app is not null")
    private String app;

    @NotBlank(message = "version is not null")
    private String version;

    @Enum(enumClass = Platform.class, method = "getName", message = "platform is invalid")
    private String platform;
}
