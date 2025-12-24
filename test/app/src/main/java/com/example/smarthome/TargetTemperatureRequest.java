package com.example.test;

public class TargetTemperatureRequest {
    private int target_temperature;

    // Конструктор с валидацией
    public TargetTemperatureRequest(int targetTemperature) {
        if (targetTemperature > 16 && targetTemperature < 30) {
            this.target_temperature = targetTemperature;
        }
    }

    // Геттер
    public int getTarget_temperature() {
        return target_temperature;
    }

    // Сеттер с валидацией
    public void setTarget_temperature(int target_temperature) {
        if (target_temperature < 16 || target_temperature > 30) {
            throw new IllegalArgumentException("Target temperature must be between 16 and 30");
        }
        this.target_temperature = target_temperature;
    }
}
