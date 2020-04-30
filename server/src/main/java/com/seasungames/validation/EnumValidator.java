package com.seasungames.validation;

import io.netty.util.internal.StringUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;

/**
 * Created by jianghaitao on 2020/4/26.
 */
public class EnumValidator implements ConstraintValidator<Enum, String> {

    private Enum annotation;

    @Override
    public void initialize(Enum constraintAnnotation) {
        annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtil.isNullOrEmpty(value)) {
            return false;
        }
        var enumClass = annotation.enumClass();
        var enumValues = enumClass.getEnumConstants();
        try {
            Method method = enumClass.getMethod(annotation.method());
            for (var o : enumValues) {
                if (value.equals(method.invoke(o))) {
                    return true;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return false;
    }
}
