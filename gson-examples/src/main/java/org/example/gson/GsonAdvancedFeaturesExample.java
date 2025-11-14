package org.example.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.example.gson.adapter.PointTypeAdapter;
import org.example.gson.model.*;
import org.example.gson.serializer.ShapeDeserializer;
import org.example.gson.serializer.ShapeSerializer;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class GsonAdvancedFeaturesExample {
    private static Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()  // Enable complex map keys
            .registerTypeAdapter(Point.class, new PointTypeAdapter())
            .registerTypeAdapter(Shape.class, new ShapeSerializer())
            .registerTypeAdapter(Shape.class, new ShapeDeserializer())
            .setPrettyPrinting()
            .create();

    private static Gson gsonWithRuntimeType = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(Point.class, new PointTypeAdapter())
            .registerTypeAdapter(Shape.class, new ShapeDeserializer()) // Register custom deserializer
            .registerTypeAdapter(Shape.class, new ShapeSerializer())   // Register custom serializer
            .setLenient()  // This will ignore unknown properties
            .setPrettyPrinting()
            .create();

    public static void main(String[] args) {

        try {
            demoComplexMapKeys();
            demoAnnotationsAndCustomSerialization();
            demoEnumWithCustomNames();
            demoRuntimeTypeAdapter();

        } catch (Exception e) {
            log.error("Error running GSON examples", e);
        }
    }


    private static void demoComplexMapKeys() {
        log.info("=== Example 1: Complex Map Keys ===");

        Map<Point, String> pointMap = new HashMap<>();
        pointMap.put(new Point(1, 2), "First point");
        pointMap.put(new Point(3, 4), "Second point");

        String jsonMap = gson.toJson(pointMap);
        log.info("Serialized map: {}", jsonMap);

        // Deserialize back to verify
        Type mapType = new TypeToken<Map<Point, String>>() {
        }.getType();
        Map<Point, String> deserializedMap = gson.fromJson(jsonMap, mapType);
        log.info("Deserialized map: {}", deserializedMap);
    }

    private static void demoAnnotationsAndCustomSerialization() {
        log.info("\n=== Example 2: Annotations and Custom Serialization ===");

        Map<Point, Shape> shapes = new HashMap<>();
        shapes.put(new Point(10, 20), new Circle("red", 5.0));

        Drawing drawing = new Drawing("drawing-123", shapes, Priority.HIGH);
        String drawingJson = gsonWithRuntimeType.toJson(drawing);
        log.info("Serialized drawing: {}", drawingJson);

        // Deserialize back to verify
        Drawing deserializedDrawing = gsonWithRuntimeType.fromJson(drawingJson, Drawing.class);
        log.info("Deserialized drawing: {}", deserializedDrawing);
    }

    private static void demoEnumWithCustomNames() {
        log.info("\n=== Example 3: Enums with Custom Names ===");

        log.info("Serialized Priority.HIGH: {}", gson.toJson(Priority.HIGH));
        log.info("Serialized Priority.MEDIUM: {}", gson.toJson(Priority.MEDIUM));

        String highJson = "\"high\"";
        Priority high = gson.fromJson(highJson, Priority.class);
        log.info("Deserialized 'high' to: {}", high);

        String mediumJson = "\"medium\"";
        Priority medium = gson.fromJson(mediumJson, Priority.class);
        log.info("Deserialized 'medium' to: {}", medium);
    }

    private static void demoRuntimeTypeAdapter() {
        log.info("\n=== Example 4: RuntimeTypeAdapterFactory ===");

        List<Shape> shapeList = Arrays.asList(
                new Circle("red", 5.0),
                new Circle("blue", 10.0)
        );

        String shapesJson = gsonWithRuntimeType.toJson(shapeList);
        log.info("Serialized shapes with type information: {}", shapesJson);

        Type shapeListType = new TypeToken<List<Shape>>() {
        }.getType();
        List<Shape> deserializedShapes = gsonWithRuntimeType.fromJson(shapesJson, shapeListType);
        log.info("Deserialized shapes: {}", deserializedShapes);

        for (Shape shape : deserializedShapes) {
            log.info("Shape type: {}, Area: {}",
                    shape.getClass().getSimpleName(),
                    shape.getArea());
        }
    }
}
