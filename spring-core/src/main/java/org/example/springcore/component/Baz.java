package org.example.springcore.component;

public class Baz {

    private final String message;

    public Baz(String message) {
        this.message = message;
        System.out.println("Baz created! Message = " + message);
    }
}
