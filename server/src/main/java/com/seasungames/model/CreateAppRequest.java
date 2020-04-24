package com.seasungames.model;

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
public class CreateAppRequest extends Request {
    @NotBlank(message = "app is not null")
    private String app;

    @NotBlank(message = "alias is not null")
    private String alias;

    @NotBlank(message = "description is not null")
    private String description;
}
