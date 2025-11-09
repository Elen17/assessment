package com.example.serializer;

import com.example.model.Customer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class CustomerSerializer extends StdSerializer<Customer> {
    public CustomerSerializer(Class<Customer> clazz) {
        super(clazz);
    }

    @Override
    public void serialize(Customer customer, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("ID", customer.getId());
        // totalAmount
        jsonGenerator.writeStringField("name", String.format("%s %s", customer.getFirstName(), customer.getLastName()));
        // customer
        jsonGenerator.writeStringField("email", customer.getEmail());
        // items
        jsonGenerator.writeObjectField("addresses", customer.getShippingAddress());
        // totalAmount
        jsonGenerator.writeEndObject();

    }
}
