package com.wotos.wotosplayerservice.validation;

import com.wotos.wotosplayerservice.validation.constraints.PlayerSearch;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import java.lang.reflect.Array;

@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class PlayerSearchValidator implements ConstraintValidator<PlayerSearch, Object[]> {

    @Override
    public boolean isValid(Object[] value, ConstraintValidatorContext constraintValidatorContext) {
        int length = Array.getLength(value[0]);

        if (value[3].equals("startswith") && length == 1 || value[3].equals("exact")) {
            return true;
        }

        return false;
    }

}
