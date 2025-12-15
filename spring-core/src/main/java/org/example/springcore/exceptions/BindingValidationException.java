package org.example.springcore.exceptions;

import org.springframework.validation.BindingResult;

public class BindingValidationException extends RuntimeException {
    private final BindingResult bindingResult;

    public BindingValidationException(BindingResult bindingResult) {
        super(bindingResult.getAllErrors().toString());
        this.bindingResult = bindingResult;
    }

    public BindingResult getBindingResult() {
        return bindingResult;
    }
}