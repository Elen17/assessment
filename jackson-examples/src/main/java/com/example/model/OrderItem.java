package com.example.model;

import com.example.helper.PrettyStringHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.Objects;

@Setter
@Getter
public class OrderItem {
    private String productId;
    private String productName;
    private int quantity;
    private double unitPrice;

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (OrderItem) obj;
        return Objects.equals(this.productId, that.productId) &&
                Objects.equals(this.productName, that.productName) &&
                this.quantity == that.quantity &&
                Double.doubleToLongBits(this.unitPrice) == Double.doubleToLongBits(that.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productName, quantity, unitPrice);
    }

    @Override
    @SneakyThrows
    public String toString() {
        return PrettyStringHelper.toPrettyString(this);
    }

}
