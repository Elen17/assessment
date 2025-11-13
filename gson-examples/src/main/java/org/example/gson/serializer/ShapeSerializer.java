package org.example.gson.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import org.example.gson.model.*;
import com.google.gson.JsonSerializationContext;

public class ShapeSerializer implements JsonSerializer<Shape> {
    @Override
    public JsonElement serialize(Shape src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        // Add common properties
        jsonObject.addProperty("color", src.getColor());

        // Add specific properties based on the shape type
        if (src instanceof Circle) {
            Circle circle = (Circle) src;
            jsonObject.addProperty("radius", circle.getRadius());
        }

        return jsonObject;
    }
}
