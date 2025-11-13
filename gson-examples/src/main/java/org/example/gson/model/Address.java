package org.example.gson.model;

import lombok.Getter;
import lombok.Setter;
import static org.example.gson.util.JsonUtils.objectToPrettyJson;

@Setter
@Getter
public class Address {
    private String street;
    private String city;

    @Override
    public String toString() {
        return objectToPrettyJson(this);
    }
}
