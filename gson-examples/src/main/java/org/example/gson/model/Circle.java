package org.example.gson.model;
import org.example.gson.util.JsonUtils;


public class Circle extends Shape {
    private double radius;
    
    public Circle(String color, double radius) {
        super(color);
        this.radius = radius;
    }
    
    @Override
    public double getArea() {
        return Math.PI * radius * radius;
    }
    
    public double getRadius() {
        return radius;
    }
    
    @Override
    public String toString() {
        return JsonUtils.objectToPrettyJson(this);
    }
}
