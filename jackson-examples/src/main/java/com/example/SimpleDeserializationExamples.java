package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import com.example.model.Name;
import com.example.model.Person;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Slf4j
public class SimpleDeserializationExamples {
    public static final String FIRST_NAME_S_LAST_NAME_S_N = "First name: {}, Last name: {}\n";

    private static final String NAME_SIMPLE_JSON = """
            {
                "firstName": "Jane",
                "lastName": "Doe"
            }
            """.stripIndent();

    private static final String NAME_LIST_JSON = """
            [
                {
                    "firstName": "James",
                    "lastName": "Mayer"
                },
                {
                    "firstName": "Audrey",
                    "lastName": "Bolton"
                }
            ]
            """.stripIndent();

    private static final String PERSON_SIMPLE_JSON = """
            {
              "name": "John Doe",
              "email": "john.doe@example.com",
              "birthDate": "1990-01-01"
            }
            """.stripIndent();

    private static final String NAME_FILE = "name.json";
    private static final String INVALID_PERSON_FILE = "invalid_person.json";
    private static final String NAME_LIST_FILE = "nameList.json";

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] args) {

        try {
            // Basic deserialization examples
            deserializeFromFile();
            deserializeFromStringToMap();
            deserializePersonString();
            deserializeJsonArray();
            deserializeUsingReader();
            deserializeToGenericList();

            // Error handling examples
            demonstrateErrorHandling();
            demonstrateIgnoreUnknownProperties();

            // Handle invalid JSON
            demonstrateInvalidJson(mapper, Path.of(ClassLoader.getSystemResource(INVALID_PERSON_FILE).toURI()));

        } catch (IOException | URISyntaxException e) {
            log.error("An error occurred: {}", e.getMessage(), e);
        }

    }

    private static void deserializePersonString() {
        log.info("=== Deserializing person string ===");
        try {
            Person person = mapper.readValue(PERSON_SIMPLE_JSON, Person.class);
            log.info("Person: {}", person);
        } catch (JsonProcessingException e) {
            log.error("Error while deserializing from string: {}", e.getMessage(), e);
        }
    }

    private static void deserializeFromFile() throws IOException, URISyntaxException {
        log.info("=== Deserializing from file: {} ===\n", NAME_FILE);
        Path filePath = Path.of(ClassLoader.getSystemResource(NAME_FILE).toURI());
        Name name = mapper.readValue(filePath.toFile(), Name.class);
        log.info("Name: {}", name);
    }

    private static void deserializeFromStringToMap() {
        log.info("=== Deserializing from string ===\n");
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> nameMap = (Map<String, String>) mapper.readValue(NAME_SIMPLE_JSON, Map.class);
            log.info(FIRST_NAME_S_LAST_NAME_S_N,
                    nameMap.get("firstName"), nameMap.get("lastName"));
        } catch (JsonProcessingException e) {
            log.error("Error while deserializing from string: {}", e.getMessage(), e);
        }
    }

    private static void deserializeJsonArray() {
        log.info("=== Deserializing JSON array ===\n");

        try {
            Path filePath = Path.of(ClassLoader.getSystemResource(NAME_LIST_FILE).toURI());
            Name[] names = mapper.readValue(filePath.toFile(), Name[].class);
            for (Name name : names) {
                log.info(name.toString());
            }
        } catch (IOException | URISyntaxException e) {
            log.error("Error while deserializing JSON array: {}", e.getMessage(), e);
        }

    }

    private static void deserializeUsingReader() {
        log.info("=== Deserializing using Reader ===\n");
        try (Reader reader = new StringReader(NAME_SIMPLE_JSON)) {
            Name name = mapper.readValue(reader, Name.class);
            log.info("Name: {}", name);
        } catch (IOException e) {
            log.error("Error while deserializing from Reader: {}", e.getMessage(), e);
        }
    }

    private static void deserializeToGenericList() throws JsonProcessingException {
        log.info("=== Deserializing to generic List ===\n");
        List<Name> names = mapper.readValue(NAME_LIST_JSON, new TypeReference<>() {
        });
        for (Name name : names) {
            log.info("Name: {}", name);
        }
    }

    private static void demonstrateErrorHandling() throws URISyntaxException, IOException {
        log.info("=== Demonstrating error handling ===\n");
        Path invalidFilePath = Path.of(ClassLoader.getSystemResource(INVALID_PERSON_FILE).toURI());

        try (Reader invalidReader = new FileReader(invalidFilePath.toFile())) {
            Name invalidName = mapper.readValue(invalidReader, Name.class);
            log.info("Name: {}", invalidName);
        } catch (JsonProcessingException e) {
            log.warn("Expected error while deserializing: {}", e.getMessage());
        }
    }

    private static void demonstrateIgnoreUnknownProperties() throws URISyntaxException, IOException {
        log.info("=== Demonstrating ignore unknown properties ===\n");
        Path invalidFilePath = Path.of(ClassLoader.getSystemResource(INVALID_PERSON_FILE).toURI());

        // Configure mapper to ignore unknown properties
        ObjectMapper lenientMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try (Reader invalidReader = new FileReader(invalidFilePath.toFile())) {
            Name invalidName = lenientMapper.readValue(invalidReader, Name.class);
            log.info("Successfully deserialized with unknown properties ignored:");
            log.info("Name: {}", invalidName);
        } catch (JsonProcessingException e) {
            log.error("Error while deserializing with unknown properties: {}", e.getMessage(), e);
        }
    }

    /**
     * Helper method to demonstrate error handling for invalid JSON.
     *
     * @param mapper   the ObjectMapper to use for deserialization
     * @param filePath path to the file containing invalid JSON
     */
    private static void demonstrateInvalidJson(ObjectMapper mapper, Path filePath) {
        try (Reader invalidReader = new FileReader(filePath.toFile())) {
            Name invalidName = mapper.readValue(invalidReader, Name.class);
            log.info("Unexpected success - should have failed: {}", invalidName);
        } catch (JsonProcessingException e) {
            log.error("Expected error while deserializing invalid JSON: {}", e.getMessage());
        } catch (IOException e) {
            log.error("IO error while reading file: {}", e.getMessage());
        }
    }
}
