package com.maersk.container.booking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class OneOfIntValidator implements ConstraintValidator<OneOfInt, Integer> {
    private int[] allowed;

    @Override
    public void initialize(OneOfInt annotation) {
        this.allowed = annotation.value();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext ctx) {
        if (value == null) return true;
        return Arrays.stream(allowed).anyMatch(v -> v == value);
    }
}
