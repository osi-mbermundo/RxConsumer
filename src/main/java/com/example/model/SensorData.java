package com.example.model;

public record SensorData(
        String id,
        double temperature,
        double humidity,
        String timestamp) {
}
