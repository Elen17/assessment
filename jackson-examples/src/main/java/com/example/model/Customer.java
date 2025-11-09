package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.Objects;

import static com.example.helper.PrettyStringHelper.toPrettyString;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private Address shippingAddress;

    public Customer() {
    }

    public Customer(
            String id,
            String fullName,
            String email,
            Address shippingAddress) {
        this.id = id;
        String[] names = fullName != null ? fullName.split("\\s+", 2) : new String[0];

        this.firstName = names.length > 0 ? names[0] : "";
        this.lastName = names.length > 1 ? names[1] : "";

        this.email = email;
        this.shippingAddress = shippingAddress;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Customer) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.firstName, that.firstName) &&
                Objects.equals(this.lastName, that.lastName) &&
                Objects.equals(this.email, that.email) &&
                Objects.equals(this.shippingAddress, that.shippingAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, email, shippingAddress);
    }

    @Override
    @SneakyThrows
    public String toString() {
        return toPrettyString(this);
    }

}
