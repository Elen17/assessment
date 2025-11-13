package com.example.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PrettyStringHelper {

    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Converts any Java object to a pretty-printed JSON string
     * @param object The object to convert
     * @return Pretty-printed JSON string
     * @throws JsonProcessingException if there's an error processing the object
     */
    public static String toPrettyString(Object object) throws JsonProcessingException {
        return mapper.writeValueAsString(object);
    }

}
