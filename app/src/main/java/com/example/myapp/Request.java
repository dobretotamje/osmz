package com.example.myapp;

import java.io.BufferedReader;
import java.io.IOException;

public class Request {
    private final RequestType type;
    private final String path;
    private final String raw;

    private Request(RequestType type, String path, String raw) {
        this.type = type;
        this.path = path;
        this.raw = raw;
    }

    public static Request parse(BufferedReader in) throws IOException {
        String buffer = in.readLine();
        if (buffer == null) return null;
        while (!buffer.isEmpty()) {

            if (buffer.contains("GET /")) {
                String substring = buffer.substring(5);
                substring = substring.substring(0, substring.lastIndexOf(" "));
                substring = substring.split(" ")[0];
                return new Request(RequestType.GET, substring, buffer);
            }
            buffer = in.readLine();
        }
        return null;
    }

    public RequestType getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return raw;
    }
}
