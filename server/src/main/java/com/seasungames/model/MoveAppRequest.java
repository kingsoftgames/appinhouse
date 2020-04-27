package com.seasungames.model;

import com.seasungames.constant.MoveOperation;
import com.seasungames.validation.EnumValidation;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * Created by jianghaitao on 2020/4/26.
 */
@Getter
@Setter
@RegisterForReflection
public class MoveAppRequest extends Request {
    @NotBlank(message = "app is not null")
    private String app;

    @EnumValidation(enumClass = MoveOperation.class, method = "getOperation", message = "operation is invalid")
    private String operation;
}
