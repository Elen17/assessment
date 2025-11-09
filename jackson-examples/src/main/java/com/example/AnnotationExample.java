package com.example;

import com.example.model.PersonExtended;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AnnotationExample {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String... args) {
        try {
            deserializeBasicPerson();

            deserializeWithCustomCreator();

            serializeWithAllFields();

            handleUnknownProperties();

        } catch (Exception e) {
            log.error("An error occurred: {}", e.getMessage(), e);
        }
    }

    private static void deserializeBasicPerson() throws JsonProcessingException {
        log.info("\n=== Example 1: Basic Deserialization ===");

        String personJson = """
                {
                    "id": 123,
                    "fullName": "John Doe",
                    "enabled": "true",
                    "email": "john.doe@example.com",
                    "dateOfBirth": "1990-01-15",
                    "password": "secret",
                    "extraField": "This will be ignored"
                }
                """;

        PersonExtended person = objectMapper.readValue(personJson, PersonExtended.class);
        log.info("Deserialized Person: {}", person.toString());
    }

    private static void deserializeWithCustomCreator() throws JsonProcessingException {
        log.info("\n=== Example 2: Deserialize with @JsonCreator ===");

        /*
          Jackson will use the @JsonCreator constructor/builder to create the object
          as it has all the required fields
         */
        String minimalJson = """
                {
                    "id": 456,
                    "fullName": "Jane Smith",
                    "enabled": "false"
                }
                """;

        PersonExtended person = objectMapper.readValue(minimalJson, PersonExtended.class);
        log.info("Minimal Person (using @JsonCreator): {}", person);
    }

    private static void serializeWithAllFields() throws JsonProcessingException {
        log.info("\n=== Example 3: Serialize with all fields ===");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("department", "Engineering");
        metadata.put("role", "Developer");

        PersonExtended person = PersonExtended.builder()
                .personId(789)
                .name("Alice Johnson")
                .enabled(true)
                .emailAddress("alice.johnson@example.com")
                .dateOfBirth(LocalDate.of(1985, 5, 20))
                .password("anotherSecret")
                .metadata(metadata)
                .build();

        String json = objectMapper.writeValueAsString(person);
        log.info("Serialized Person:\n{}", json);
    }

    private static void handleUnknownProperties() {
        log.info("\n=== Example 4: Handle unknown properties ===");

        String jsonWithUnknownFields = """
                {
                    "id": 999,
                    "fullName": "Bob Brown",
                    "enabled": "true",
                    "unknownField1": "value1",
                    "unknownField2": 12345
                }
                """;

        try {
            // This will work because we have @JsonAnySetter in Person class
            PersonExtended person = objectMapper.readValue(jsonWithUnknownFields, PersonExtended.class);
            log.info("Person with unknown fields deserialized: {}", person);
            log.info("Unknown fields stored in metadata: {}", person.getMetadata());
        } catch (Exception e) {
            log.error("Error deserializing with unknown fields", e);
        }
    }
}