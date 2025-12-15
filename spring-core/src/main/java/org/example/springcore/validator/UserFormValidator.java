package org.example.springcore.validator;

import org.example.springcore.model.UserForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class UserFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return UserForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "email.empty");

        UserForm form = (UserForm) target;

        if (form.getUsername() != null &&
                form.getUsername().toLowerCase().contains("admin")) {
            errors.rejectValue(
                    "username",
                    "username.forbidden",
                    "Username cannot contain 'admin'"
            );
        }
    }
}
