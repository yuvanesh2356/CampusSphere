package com.campussphere.common.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Validation logic behind @ValidCollegeEmail. Reads the allowed domain
 * suffix from application.properties so it can be changed per-institution
 * without touching code.
 */
@Component
public class CollegeEmailValidator implements ConstraintValidator<ValidCollegeEmail, String> {

    @Value("${campussphere.security.allowed-email-domain}")
    private String allowedDomain;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            // Let @NotBlank handle emptiness; this validator only checks domain.
            return true;
        }
        return email.trim().toLowerCase().endsWith(allowedDomain.toLowerCase());
    }
}
