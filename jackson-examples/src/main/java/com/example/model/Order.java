package com.example.model;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.example.helper.PrettyStringHelper.toPrettyString;

@Setter
@Getter
public class Order {
    private String orderId;
    private Customer customer;
    private List<OrderItem> items;
    private double totalAmount;
    private Date orderDate;


    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Order) obj;
        return Objects.equals(this.orderId, that.orderId) &&
                Objects.equals(this.customer, that.customer) &&
                Objects.equals(this.items, that.items) &&
                Double.doubleToLongBits(this.totalAmount) == Double.doubleToLongBits(that.totalAmount) &&
                Objects.equals(this.orderDate, that.orderDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, customer, items, totalAmount, orderDate);
    }

    @Override
    @SneakyThrows
    public String toString() {
        return toPrettyString(this);
    }

}
