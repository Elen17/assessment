package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Value;

// Add this as a nested class at the end of the GuavaCollectionsExamples class
@Value
@AllArgsConstructor
public class Product {
    String name;
    double price;
    double rating;  // 1.0 to 5.0
    int salesCount;
}
