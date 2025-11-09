package com.example.model;

import com.example.deserializer.OptimizedBooleanDeserializer;
import com.example.helper.PrettyStringHelper;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Map;

import static com.example.helper.PrettyStringHelper.toPrettyString;

@Data
@Builder
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"personId", "fullName", "email", "dateOfBirth", "enabled", "metadata"})
public class PersonExtended {
    @JsonProperty("id")
    private final long personId;

    @JsonProperty("fullName")
    private final String name;

    @JsonDeserialize(using = OptimizedBooleanDeserializer.class)
    private final Boolean enabled;

    @JsonIgnore
    private final String password;  // Will be ignored during serialization

    @JsonProperty("email")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final String emailAddress;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate dateOfBirth;

    @JsonAnySetter
    private Map<String, Object> metadata;

    @JsonAnyGetter
    public Map<String, Object> getMetadata() {
        return metadata;
    }

//    @JsonValue
//    /*
//    this method will be used to serialize the object to JSON ()
//     */
//    public String toCustomJson() {
//        return String.format("{\"id\":%d,\"name\":\"%s\"}", personId, name);
//    }

    @JsonCreator
    public static PersonExtended create(
            @JsonProperty("id") long id,
            @JsonProperty("fullName") String name,
            @JsonProperty("enabled") Boolean enabled) {
        return PersonExtended.builder()
                .personId(id)
                .name(name)
                .enabled(enabled)
                .build();
    }

    @Override
    @SneakyThrows
    public String toString() {
        return toPrettyString(this);
    }
}