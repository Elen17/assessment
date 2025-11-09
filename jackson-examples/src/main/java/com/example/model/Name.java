package com.example.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Name {
    private String firstName;
    private String lastName;

    @Override
    public String toString() {
        return "{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
