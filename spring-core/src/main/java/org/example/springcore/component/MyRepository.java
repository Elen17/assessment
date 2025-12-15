package org.example.springcore.component;

import java.util.List;

public class MyRepository {

    public List<String> findAll() {
        return List.of("A", "B", "C");
    }
}

