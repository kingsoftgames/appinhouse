package com.seasungames.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by jianghaitao on 2020/4/24.
 */
@Getter
@Setter
@RegisterForReflection
public class Request {

    @JsonIgnoreProperties
    protected String paramErrorMessage;

    public boolean isInvalid(Validator validator) {
        var violations = validator.validate(this);
        paramErrorMessage = getParamsErrorMessage(violations);
        return !violations.isEmpty();
    }

    private static <T> String getParamsErrorMessage(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
    }
}
