package org.example.gson.strategy;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class PersonLastNameFieldExclusionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
        return fieldAttributes.getName().equals("lastName");
    }

    @Override
    public boolean shouldSkipClass(Class<?> aClass) {
        return false;
    }
}
