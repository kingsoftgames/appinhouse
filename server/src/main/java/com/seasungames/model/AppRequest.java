package com.seasungames.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * Created by jianghaitao on 2020/4/26.
 */
@Getter
@Setter
@Builder
@RegisterForReflection
public class AppRequest extends Request {
    @NotBlank(message = "app is not null")
    private String app;
}
