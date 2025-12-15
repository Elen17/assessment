package org.example.springcore.component;

import jakarta.annotation.PreDestroy;

public class Foo {
    public Foo() {
        System.out.println("Foo created!");
    }

    @PreDestroy
    public void close() {
        System.out.println("Foo Destroy method!!!");
    }
}

