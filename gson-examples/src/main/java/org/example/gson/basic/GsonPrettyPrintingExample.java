package org.example.gson.basic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.gson.model.Person;
import org.example.gson.model.Address;
import org.example.gson.adapter.LocalDateAdapter;
import java.time.LocalDate;

public class GsonPrettyPrintingExample {
    public static void main(String[] args) {
        Gson gsonPretty = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();

        Address address = new Address();
        address.setCity("New York City");
        address.setStreet("Times Square");
        Person person = new Person();
        person.setFirstName("John");
        person.setLastName("Doe");
        person.setDateOfBirth(LocalDate.of(1990, 1, 1));
        person.setAddress(address);
        System.out.println(gsonPretty.toJson(person));
    }
}
