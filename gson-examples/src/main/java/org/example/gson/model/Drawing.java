package org.example.gson.model;

import com.google.gson.annotations.SerializedName;
import org.example.gson.util.JsonUtils;

import java.util.Map;

public class Drawing {
    @SerializedName("drawingId")
    private final String id;

    @SerializedName("shapes")
    private final Map<Point, Shape> shapes;

    @SerializedName("priority")
    private final Priority priority;

    public Drawing(String id, Map<Point, Shape> shapes, Priority priority) {
        this.id = id;
        this.shapes = shapes;
        this.priority = priority;
    }

    @Override
    public String toString() {
        return JsonUtils.objectToPrettyJson(this);
    }
}

