package com.wotos.wotosplayerservice.validation.constraints;

import com.wotos.wotosplayerservice.validation.PlayerSearchValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Constraint(validatedBy = PlayerSearchValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PlayerSearch {
    String message() default "Invalid Search Type: '${validatedValue[3]}' must be 'exact' with multiple nicknames or 'startswith' with one nickname";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
