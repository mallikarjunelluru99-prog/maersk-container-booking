package com.maersk.container.booking.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OneOfIntValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface OneOfInt {
    int[] value();
    String message() default "must be one of {value}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
