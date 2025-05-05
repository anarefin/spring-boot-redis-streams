package com.example.demo.model;

import java.io.Serializable;
import java.time.Instant;

public class DummyData implements Serializable {
    private String id;
    private String message;
    private Instant timestamp;

    // Default constructor (needed for serialization/deserialization)
    public DummyData() {
    }

    public DummyData(String id, String message) {
        this.id = id;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "DummyData{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
} 