package com.example.model;

import com.example.helper.PrettyStringHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.Objects;

@Setter
@Getter
public class Address {
    private String street;
    private String city;
    private String zipCode;
    private String country;

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Address) obj;
        return Objects.equals(this.street, that.street) &&
                Objects.equals(this.city, that.city) &&
                Objects.equals(this.zipCode, that.zipCode) &&
                Objects.equals(this.country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, zipCode, country);
    }

    @SneakyThrows
    @Override
    public String toString() {
        return PrettyStringHelper.toPrettyString(this);
    }

}
