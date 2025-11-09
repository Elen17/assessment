package org.example.gson.basic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.gson.model.Address;
import org.example.gson.model.Person;
import org.example.gson.adapter.LocalDateAdapter;
import java.time.LocalDate;

import static java.lang.System.out;

public class GsonBasicExample {
    public static void main(String[] args) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();

        String json = """
            {"firstName":"John","lastName":"Doe","dateOfBirth":"1990-01-01"}
        """;
        Person person = gson.fromJson(json, Person.class);
        out.printf("First name: %s%nLast name: %s%n", person.getFirstName(), person.getLastName());

        Address address = new Address();
        address.setCity("New York City");
        address.setStreet("Times Square");
        person.setAddress(address);
        out.println(gson.toJson(person));
    }
}
