package com.seasungames.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Created by jianghaitao on 2020/4/30.
 */
public class NotNullIfAnotherFieldHasValueValidator
        implements ConstraintValidator<NotNullIfAnotherFieldHasValue, Object> {

    private String fieldName;
    private String expectedFieldValue;
    private String dependFieldName;
    private boolean equal;

    @Override
    public void initialize(NotNullIfAnotherFieldHasValue annotation) {
        fieldName = annotation.fieldName();
        expectedFieldValue = annotation.fieldValue();
        dependFieldName = annotation.dependFieldName();
        equal = annotation.equal();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext ctx) {
        try {
            String fieldValue = getProperty(value, fieldName);
            String dependFieldValue = getProperty(value, dependFieldName);
            return isValid(equal, expectedFieldValue, fieldValue, dependFieldValue);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isValid(boolean equal, String expectedFieldValue, String fieldValue, String dependFieldValue) {
        return equal ? isValidWithValue(expectedFieldValue, fieldValue, dependFieldValue) :
                isValidWithoutValue(fieldValue, dependFieldValue);
    }

    private static boolean isValidWithValue(String expectedFieldValue, String fieldValue, String dependFieldValue) {
        return !(expectedFieldValue.equals(fieldValue) && Objects.isNull(dependFieldValue));
    }

    private static boolean isValidWithoutValue(String fieldValue, String dependFieldValue) {
        return !(Objects.nonNull(fieldValue) && Objects.isNull(dependFieldValue));
    }

    // 不考虑继承的属性
    private static String getProperty(Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (String) field.get(object);
    }
}