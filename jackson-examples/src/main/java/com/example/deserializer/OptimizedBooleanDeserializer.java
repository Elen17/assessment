package com.example.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class OptimizedBooleanDeserializer extends JsonDeserializer<Boolean> {

    private static final Set<String> TRUE_VALUES = new HashSet<>(Arrays.asList(
            "true", "yes", "y", "1"
    ));

    private static final Set<String> FALSE_VALUES = new HashSet<>(Arrays.asList(
            "false", "no", "n", "0"
    ));

    @Override
    public Boolean deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        String value = p.getText().trim().toLowerCase();

        if (TRUE_VALUES.contains(value)) {
            return Boolean.TRUE;
        }

        if (FALSE_VALUES.contains(value)) {
            return Boolean.FALSE;
        }

        if (value.isEmpty()) {
            log.warn("Empty boolean value encountered, defaulting to null");
            return null;
        }

        String errorMsg = String.format(
                "Cannot deserialize value of type `java.lang.Boolean` from String \"%s\": " +
                        "only the following values are accepted: %s (case-insensitive)",
                p.getText(),
                String.join(", ", TRUE_VALUES) + " or " + String.join(", ", FALSE_VALUES)
        );

        throw new InvalidFormatException(p, errorMsg, value, Boolean.class);
    }

    @Override
    public Boolean getNullValue(DeserializationContext ctxt) {
        log.debug("Received null value, returning null");
        return null;
    }
}