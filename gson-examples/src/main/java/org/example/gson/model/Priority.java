package org.example.gson.model;

import com.google.gson.annotations.SerializedName;

public enum Priority {
    @SerializedName("low")
    LOW("Low Priority"),
    
    @SerializedName("medium")
    MEDIUM("Medium Priority"),
    
    @SerializedName("high")
    HIGH("High Priority"),
    
    @SerializedName("critical")
    CRITICAL("Critical Priority");
    
    private final String displayName;
    
    Priority(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
