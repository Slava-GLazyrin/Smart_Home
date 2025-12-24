package com.example.test;

public class TemperatureResponse {

    private int temperature;

    public int getTemperature() {
        // Проверяем, что температура в допустимом диапазоне
        if (temperature < -50 || temperature > 60) {
            throw new IllegalArgumentException("Invalid temperature value: " + temperature);
        }
        return temperature;
    }
}
