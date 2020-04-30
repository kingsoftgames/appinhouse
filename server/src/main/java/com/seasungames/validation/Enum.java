package com.seasungames.validation;

import com.seasungames.validation.Enum.List;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by jianghaitao on 2020/4/26.
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = {EnumValidator.class})
@Repeatable(List.class)
public @interface Enum {

    /**
     * 枚举的class类型
     */
    Class<? extends java.lang.Enum> enumClass();

    String message() default "invalid Enum Value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 被调用的枚举属性的方法名，值用来判断是否合法
     */
    String method() default "ordinal";

    @Documented
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @interface List {
        Enum[] value();
    }
}
