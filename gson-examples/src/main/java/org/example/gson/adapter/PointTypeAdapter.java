package org.example.gson.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.example.gson.model.Point;

import java.io.IOException;

public class PointTypeAdapter extends TypeAdapter<Point> {
    @Override
    public void write(JsonWriter out, Point value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(String.format("%d,%d", value.getX(), value.getY()));
    }

    @Override
    public Point read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String[] parts = in.nextString().split(",");
        return new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
}

