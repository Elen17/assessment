package org.example.gson.adapter;

import com.google.gson.InstanceCreator;
import org.example.gson.model.Person;

import java.lang.reflect.Type;
import java.time.LocalDate;

public class PersonCreator implements InstanceCreator<Person> {

    @Override
    public Person createInstance(Type type) {
        Person person = new Person();
        person.setDateOfBirth(LocalDate.of(1970, 1, 1));
        return person;
    }
}
