package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.example.deserializer.OrderDeserializer;
import lombok.extern.slf4j.Slf4j;
import com.example.model.Order;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Objects;

@Slf4j
public class CustomDeserializationExample {

    public static void main(String... args) {
        try {
            // Initialize and configure ObjectMapper
            ObjectMapper objectMapper = createAndConfigureObjectMapper();

            // Deserialize Order from JSON file
            deserializeOrderFromFile(objectMapper);
        } catch (Exception e) {
            log.error("An unexpected error occurred in the demo", e);
            System.exit(1);
        }
    }

    private static ObjectMapper createAndConfigureObjectMapper() {
        log.info("Configuring ObjectMapper with custom deserializers");

        ObjectMapper objectMapper = new ObjectMapper();

        // Create and configure module with custom deserializers
        SimpleModule simpleModule = new SimpleModule()
                .addDeserializer(Order.class, new OrderDeserializer(Order.class));

        // Configure date format
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

        // Register the module
        objectMapper.registerModule(simpleModule);

        return objectMapper;
    }

    private static void deserializeOrderFromFile(ObjectMapper objectMapper) throws Exception {
        log.info("=== Deserializing Order from file ===");

        try {
            // Get the path to the order.json file in resources
            Path orderJsonPath = Path.of(Objects.requireNonNull(
                    ClassLoader.getSystemResource("order.json")
            ).toURI());

            log.debug("Reading order from file: {}", orderJsonPath);

            // Deserialize the order
            Order order = objectMapper.readValue(orderJsonPath.toFile(), Order.class);
            log.info("Successfully deserialized Order:\n {}", order);

        } catch (Exception e) {
            log.error("Failed to deserialize Order from file", e);
            throw e;
        }
    }
}