package org.example.springcore.converter;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class MyStringToDateConverter implements Converter<String, Date> {
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public @Nullable Date convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            log.debug("Received null or empty date string");
            return null;
        }

        String trimmedSource = source.trim();

        // Try parsing as formatted date first
        try {
            return FORMATTER.parse(trimmedSource);
        } catch (ParseException e) {
            log.debug("Failed to parse date string '{}' as formatted date, trying as timestamp", trimmedSource);
        }

        // Fallback to parsing as timestamp
        try {
            long timestamp = Long.parseLong(trimmedSource);
            Date date = new Date(timestamp);
            log.debug("Successfully parsed timestamp: {}", date);
            return date;
        } catch (NumberFormatException e) {
            log.warn("Failed to parse date string '{}' as either formatted date or timestamp", trimmedSource);
            return null;
        }
    }
}
