package org.example.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static java.lang.System.out;


public class JsonPipelineExample {
    public static void main(String[] args) {
        String inputJson = """
                [
                  {"id": 1, "type": "INFO", "message": "Server started"},
                  {"id": 2, "type": "ERROR", "message": "Connection failed"},
                  {"id": 3, "type": "INFO", "message": "User logged in"}
                ]
                """;

        try (
                JsonReader reader = new JsonReader(new StringReader(inputJson));
                StringWriter stringWriter = new StringWriter();
                JsonWriter writer = new JsonWriter(stringWriter)
        ) {
            JsonParser jsonParser = new JsonParser();

            // Step 1: Begin reading input array
            reader.beginArray();
            writer.beginArray(); // Prepare output array

            while (reader.hasNext()) {
                // Step 2: Parse current object using JsonParser
                JsonElement element = jsonParser.parse(reader);
                JsonObject obj = element.getAsJsonObject();

                // Step 3: Modify the object
                modifyLogEntry(obj);

                // Step 4: Write modified object to output
                Gson gson = new Gson();
                gson.toJson(obj, writer); // Writes JSON directly using writer
            }

            reader.endArray();
            writer.endArray();

            // Output final transformed JSON
            out.println("✅ Modified JSON output:");
            out.println(stringWriter.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void modifyLogEntry(JsonObject obj) {
        // Example modifications:
        // 1. Add timestamp field
        obj.addProperty("timestamp", System.currentTimeMillis());

        // 2. Enrich ERROR messages with severity
        if ("ERROR".equals(obj.get("type").getAsString())) {
            obj.addProperty("severity", "HIGH");
        }

        // 3. Transform message text
        String message = obj.get("message").getAsString();
        obj.addProperty("message", message.toUpperCase());
    }
}

