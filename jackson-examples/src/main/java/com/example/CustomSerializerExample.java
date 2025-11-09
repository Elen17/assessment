package com.example;

import com.example.deserializer.OrderDeserializer;
import com.example.model.Customer;
import com.example.model.Order;
import com.example.serializer.CustomerSerializer;
import com.example.serializer.OrderSerializer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Slf4j
public class CustomSerializerExample {

    public static void main(String... args) {
        ObjectMapper objectMapper = createAndConfigureObjectMapper();

        try {
            // Deserialize single order
            Order order = objectMapper.readValue(
                    ClassLoader.getSystemResourceAsStream("order.json"),
                    Order.class
            );
            log.info("Order: {}", order);
            writeToFile(objectMapper, order, "serialized_order.json");

            // Deserialize list of orders (without custom deserializer)
            List<Order> orders = objectMapper.readValue(
                    ClassLoader.getSystemResourceAsStream("orders.json"),
                    new TypeReference<>() {}
            );
            writeToFile(objectMapper, orders, "serialized_orders.json");

            @SuppressWarnings("unchecked")
            // in case of getting List<Order> from json file; while deserializing we need to use TypeReference
            // because List.class is not a parameterized type
            // so while serializing we need to use TypeReference so our custom serializer worked
            List<Order> customSerializedOrders = objectMapper.readValue(
                    ClassLoader.getSystemResourceAsStream("orders.json"),
                    List.class
            );
            writeToFile(objectMapper, customSerializedOrders, "custom_serialized_orders.json");

        } catch (IOException | URISyntaxException e) {
            log.error("Error during serialization/deserialization", e);
            throw new RuntimeException("Failed to process JSON", e);
        }
    }

    private static ObjectMapper createAndConfigureObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Configure module with custom serializers/deserializers
        SimpleModule module = new SimpleModule()
                .addDeserializer(Order.class, new OrderDeserializer(Order.class))
                .addSerializer(Order.class, new OrderSerializer(Order.class))
                .addSerializer(Customer.class, new CustomerSerializer(Customer.class));

        // Apply configurations
        objectMapper.registerModule(module)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);

        return objectMapper;
    }

    private static void writeToFile(ObjectMapper objectMapper, Object value, String fileName)
            throws IOException, URISyntaxException {
        Path outputPath = Path.of(
                Objects.requireNonNull(
                        ClassLoader.getSystemResource("")
                ).toURI()
        ).resolve(fileName);

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(outputPath.toFile(), value);
    }
}