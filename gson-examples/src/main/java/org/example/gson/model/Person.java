package org.example.gson.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import static org.example.gson.util.JsonUtils.objectToPrettyJson;

@Getter
public class Person {
    @Setter
    private String firstName;
    @Setter
    private String lastName;
    private LocalDate dateOfBirth;
    // by applying transient keyword, this field will not be serialized
    private transient int age;

    @Setter
    private Address address;

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        // simple year difference
        this.age = LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    @Override
    public String toString() {
        return objectToPrettyJson(this);
    }
}
