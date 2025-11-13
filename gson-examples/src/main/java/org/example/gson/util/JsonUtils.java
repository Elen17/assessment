package org.example.gson.util;

import com.google.gson.*;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.example.gson.adapter.LocalDateAdapter;

import java.time.LocalDate;

@Slf4j
@UtilityClass
public class JsonUtils {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();


    public static String toPrettyJson(String json) {
        JsonElement jsonElement = JsonParser.parseString(json);
        return GSON.toJson(jsonElement);
    }

    public static String objectToPrettyJson(Object object) {
        if (object == null) {
            return "null";
        }
        return GSON.toJson(object);
    }

}
