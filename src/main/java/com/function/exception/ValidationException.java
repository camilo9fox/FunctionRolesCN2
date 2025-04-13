package com.function.exception;

import jakarta.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidationException extends RuntimeException {
    private final Set<String> violations;

    public ValidationException(Set<? extends ConstraintViolation<?>> violations) {
        super("Error de validación");
        this.violations = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    public Set<String> getViolations() {
        return violations;
    }
}
