package com.seasungames.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * Created by jianghaitao on 2020/4/24.
 */

@Getter
@Setter
@RegisterForReflection
public class ModifyAppRequest extends Request {
    @NotBlank(message = "app is not null")
    private String app;

    @NotBlank(message = "alias is not null")
    private String alias;

    @NotBlank(message = "description is not null")
    private String description;

    @JsonProperty("ios_title")
    @NotBlank(message = "ios_title is not null")
    private String iosTitle;

    @JsonProperty("ios_bundle_id")
    @NotBlank(message = "ios_bundle_id is not null")
    private String iosBundleId;
}
