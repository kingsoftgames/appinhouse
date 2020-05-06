package com.seasungames.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 两种情况：
 * 1.如果equal是true，则判断当fieldName值为fieldValue时，dependFieldName不为空
 * <p>
 * 2.如果equal是false，则判断当fieldName不为空时，dependFieldName不为空
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = {NotNullIfAnotherFieldHasValueValidator.class})
@Repeatable(NotNullIfAnotherFieldHasValue.List.class)
public @interface NotNullIfAnotherFieldHasValue {

    String fieldName();

    String fieldValue() default "";

    String dependFieldName();

    // 如果equal是false 则判断fieldName不是null
    boolean equal() default false;

    String message() default "{NotNullIfAnotherFieldHasValue.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Documented
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @interface List {
        NotNullIfAnotherFieldHasValue[] value();
    }

}