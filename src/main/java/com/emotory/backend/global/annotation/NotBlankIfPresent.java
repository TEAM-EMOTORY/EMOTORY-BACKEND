package com.emotory.backend.global.annotation;

import com.emotory.backend.global.common.NotBlankIfPresentValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotBlankIfPresentValidator.class)
public @interface NotBlankIfPresent {
    String message() default "빈 값은 허용되지 않습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
