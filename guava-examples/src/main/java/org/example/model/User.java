package org.example.model;

import org.jspecify.annotations.NonNull;

public record User(String email, String name) {

    @Override
    @NonNull
    public String toString() {
        return name + " (" + email + ")";
    }
}
