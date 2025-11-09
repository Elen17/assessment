package org.example.gson.model;

import com.google.gson.annotations.Expose;

import java.time.LocalDate;

public class Person {
    @Expose
    private String firstName;
    @Expose(serialize = false)
    private String lastName;
    @Expose
    private LocalDate dateOfBirth;
    // by applying transient keyword, this field will not be serialized
    private transient int age;

    private Address address;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        // simple year difference
        this.age = LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    public int getAge() {
        return age;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Person{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", age=" + age +
                '}';
    }
}
