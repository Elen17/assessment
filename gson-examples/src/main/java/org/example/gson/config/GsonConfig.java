package org.example.gson.config;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import org.example.gson.adapter.LocalDateAdapter;
import org.example.gson.strategy.PersonLastNameFieldExclusionStrategy;

import java.time.LocalDate;

@Slf4j
public class GsonConfig {
    private static final LocalDateAdapter LOCAL_DATE_ADAPTER = new LocalDateAdapter();

    public static Gson createDefaultGson() {
        log.debug("Creating default Gson instance");
        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, LOCAL_DATE_ADAPTER)
                .create();
    }

    public static Gson createPrettyGson() {
        log.debug("Creating pretty Gson instance");
        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, LOCAL_DATE_ADAPTER)
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setPrettyPrinting()
                .create();
    }

    public static Gson createGsonWithExclusionStrategy() {
        log.debug("Creating Gson with exclusion strategy");
        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, LOCAL_DATE_ADAPTER)
                .setExclusionStrategies(new PersonLastNameFieldExclusionStrategy())
                .setPrettyPrinting()
                .create();
    }
}
