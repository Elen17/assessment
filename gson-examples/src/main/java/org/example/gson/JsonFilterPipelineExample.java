package org.example.gson;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class JsonFilterPipelineExample {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String LOG_TYPE_FIELD = "type";
    private static final String TIMESTAMP_FIELD = "timestamp";
    private static final String MESSAGE_FIELD = "message";
    private static final String FILE_NAME = "logs.json";

    public static void main(String[] args) {
        try {
            processJson(new FileReader(ClassLoader.getSystemResource(FILE_NAME).getFile()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void processJson(FileReader file) {
        try (JsonReader reader = new JsonReader(file);
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
