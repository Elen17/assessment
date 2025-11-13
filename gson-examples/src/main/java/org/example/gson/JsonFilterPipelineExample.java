package org.example.gson;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

@Slf4j
public class JsonFilterPipelineExample {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String LOG_TYPE_FIELD = "type";
    private static final String TIMESTAMP_FIELD = "timestamp";
    private static final String MESSAGE_FIELD = "message";

    private static final String INPUT_JSON = """
            [
                {"type": "INFO", "message": "Application started", "timestamp": 1636739200000},
                {"type": "ERROR", "message": "Failed to connect to database", "timestamp": 1636739201000},
                {"type": "WARN", "message": "High memory usage", "timestamp": 1636739202000},
                {"type": "ERROR", "message": "Invalid user input", "timestamp": 1636739203000}
            ]""".stripIndent();

    public static void main(String[] args) {
        processJson(INPUT_JSON);
    }

    public static void processJson(String inputJson) {
        try (StringReader stringReader = new StringReader(inputJson);
             JsonReader reader = new JsonReader(stringReader);
             StringWriter stringWriter = new StringWriter();
             JsonWriter writer = new JsonWriter(stringWriter)) {

            processJsonStream(reader, writer);
            log.info("Filtered + Modified JSON output:\n{}", stringWriter);

        } catch (IOException e) {
            log.error("Error processing JSON: {}", e.getMessage(), e);
        }
    }

    private static void processJsonStream(JsonReader reader, JsonWriter writer) throws IOException {
        JsonParser jsonParser = new JsonParser();
        reader.beginArray();
        writer.beginArray();

        while (reader.hasNext()) {
            processJsonElement(jsonParser, reader, writer);
        }

        reader.endArray();
        writer.endArray();
    }

    private static void processJsonElement(JsonParser parser, JsonReader reader, JsonWriter writer) {
        try {
            JsonElement element = parser.parse(reader);
            JsonObject obj = element.getAsJsonObject();

            if (isErrorLogEntry(obj)) {
                JsonObject modifiedObj = modifyLogEntry(obj);
                GSON.toJson(modifiedObj, writer);
            }
        } catch (JsonParseException e) {
            log.warn("Skipping malformed JSON element: {}", e.getMessage());
        }
    }

    private static boolean isErrorLogEntry(JsonObject logEntry) {
        return logEntry.has(LOG_TYPE_FIELD) &&
               "ERROR".equalsIgnoreCase(logEntry.get(LOG_TYPE_FIELD).getAsString());
    }

    private static JsonObject modifyLogEntry(JsonObject logEntry) {
        JsonObject modified = logEntry.getAsJsonObject();
        modified.addProperty(TIMESTAMP_FIELD, System.currentTimeMillis());

        if (modified.has(MESSAGE_FIELD)) {
            String message = modified.get(MESSAGE_FIELD).getAsString();
            modified.addProperty(MESSAGE_FIELD, "[MODIFIED] " + message);
        }

        return modified;
    }
}
