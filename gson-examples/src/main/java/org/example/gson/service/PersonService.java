package org.example.gson.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.example.gson.model.Person;
import org.example.gson.model.Address;

@Slf4j
public record PersonService(Gson gson) {

    public String serializePerson(Person person) {
        log.debug("Serializing person: {}", person);
        return gson.toJson(person);
    }

    public Person deserializePerson(String json) {
        log.debug("Deserializing person from JSON: {}", json);
        return gson.fromJson(json, Person.class);
    }

    public Person createSamplePerson() {
        log.debug("Creating sample person");
        Person person = new Person();
        person.setFirstName("Jane");
        person.setLastName("Doe");

        Address address = new Address();
        address.setCity("New York City");
        address.setStreet("Times Square");
        person.setAddress(address);

        return person;
    }
}
