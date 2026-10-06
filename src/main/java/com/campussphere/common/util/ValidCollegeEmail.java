package com.campussphere.common.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bean Validation annotation that restricts a field to email addresses
 * ending in the college's official domain (configured via
 * campussphere.security.allowed-email-domain in application.properties).
 *
 * Usage: place directly on the email field of UserRegisterDTO.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CollegeEmailValidator.class)
public @interface ValidCollegeEmail {

    String message() default "Email must belong to the college domain";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
