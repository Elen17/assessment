package org.example.springcore.controller;

import jakarta.validation.Valid;
import org.example.springcore.exceptions.BindingValidationException;
import org.example.springcore.formatter.LocalDateFormatter;
import org.example.springcore.model.UserForm;
import org.example.springcore.validator.UserFormValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/users")
public class UserController {

    private final UserFormValidator userFormValidator;

    public UserController(UserFormValidator userFormValidator) {
        this.userFormValidator = userFormValidator;
    }


    /**
     * Annotation that identifies methods that initialize the org.springframework.web.bind.WebDataBinder
     * which will be used for populating command and form object arguments of annotated handler methods.
     */
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(userFormValidator);
        binder.addCustomFormatter(new LocalDateFormatter());
    }


    @PostMapping("/register")
    /**
     * @Validated is a Spring annotation that activates method-level validation
     * It tells Spring to validate method parameters before the method is executed
     * Without it, Spring won't perform any validation on the method parameters
     *
     * @Validated at class level enables method validation
     * @Valid on the parameter triggers the validation
     * */
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserForm form,
                                          BindingResult bindingResult) throws NoSuchMethodException, MethodArgumentNotValidException {
        if (bindingResult.hasErrors()) {
            throw new BindingValidationException(bindingResult);
        }
        return ResponseEntity.ok(String.format("User %s registered successfully", form.getUsername()));
    }
}

