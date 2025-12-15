package org.example.springcore.component;

public class Bar {

    private final Foo foo;

    public Bar(Foo foo) {
        this.foo = foo;
        System.out.println("Bar created with Foo dependency!");
    }
}

