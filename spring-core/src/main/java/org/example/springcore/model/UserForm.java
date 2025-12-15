package org.example.springcore.model;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * This runs AFTER binding, BEFORE controller logic.
 */
@Getter
@Setter
public class UserForm {

    @NotBlank
    private String username;

    @Min(18)
    private int age;

    @Email
    private String email;

    @NotNull
    private LocalDate birthDate;
}

