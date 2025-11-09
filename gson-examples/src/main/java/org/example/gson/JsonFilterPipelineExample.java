package org.example.gson;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;

/**
 * | Step                 | Component                                                                     | Description |
 * | -------------------- | ----------------------------------------------------------------------------- | ----------- |
 * | **1️⃣ `JsonReader`** | Reads each JSON object lazily from a stream (efficient for large arrays).     |             |
 * | **2️⃣ `JsonParser`** | Parses that single object into a `JsonObject` (so we can inspect and modify). |             |
 * | **3️⃣ Filter**       | `if (!"ERROR".equalsIgnoreCase(...)) continue;` skips unnecessary objects.    |             |
 * | **4️⃣ Modify**       | Adds `timestamp` and `severity`, uppercases message.                          |             |
 * | **5️⃣ `JsonWriter`** | Writes the filtered objects directly into the new JSON array.                 |             |
 */

public class JsonFilterPipelineExample {

    public static void main(String[] args) {
        String inputJson = """
        [
          {"id": 1, "type": "INFO", "message": "Server started"},
          {"id": 2, "type": "ERROR", "message": "Connection failed"},
          {"id": 3, "type": "INFO", "message": "User logged in"},
          {"id": 4, "type": "ERROR", "message": "Disk full"}
        ]
        """;

        try (
                JsonReader reader = new JsonReader(new StringReader(inputJson));
                StringWriter stringWriter = new StringWriter();
                JsonWriter writer = new JsonWriter(stringWriter)
        ) {
            JsonParser jsonParser = new JsonParser();
            reader.beginArray();
            writer.beginArray();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            while (reader.hasNext()) {
                // Parse next element as JsonObject
                JsonElement element = jsonParser.parse(reader);
                JsonObject obj = element.getAsJsonObject();

                // Filter: keep only ERROR logs
                if (!"ERROR".equalsIgnoreCase(obj.get("type").getAsString())) {
                    continue; // skip non-ERROR entries
                }

                // Modify the JSON object
                modifyLogEntry(obj);

                // Write filtered + modified entry
                gson.toJson(obj, writer);
            }

            reader.endArray();
            writer.endArray();
            writer.close();

            // Output filtered and modified JSON
            System.out.println("✅ Filtered + Modified JSON output:");
            System.out.println(stringWriter);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void modifyLogEntry(JsonObject obj) {
        // Add new fields
        obj.addProperty("timestamp", System.currentTimeMillis());
        obj.addProperty("severity", "HIGH");

        // Transform message
        String message = obj.get("message").getAsString();
        obj.addProperty("message", message.toUpperCase());
    }
}

