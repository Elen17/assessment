package org.example.gson.serializer;
import org.example.gson.model.*;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ShapeDeserializer implements JsonDeserializer<Shape> {

    @Override
    public Shape deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        String type = jsonObject.has("type") ? jsonObject.get("type").getAsString() : 
                     (jsonObject.has("radius") ? "circle" : null);
        
        if (type == null) {
            throw new JsonParseException("Cannot determine shape type");
        }
        
        String color = jsonObject.get("color").getAsString();

        switch (type.toLowerCase()) {
            case "circle":
                double radius = jsonObject.get("radius").getAsDouble();
                return new Circle(color, radius);
            default:
                throw new JsonParseException("Unknown shape type: " + type);
        }
    }
}
