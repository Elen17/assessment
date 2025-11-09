package com.example.model;

import com.example.helper.PrettyStringHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.Date;

@Setter
@Getter
public class Person {
    private String name;
    private String email;
    private Date birthDate;

    @Override
    @SneakyThrows
    public String toString() {
        return PrettyStringHelper.toPrettyString(this);
    }
}
