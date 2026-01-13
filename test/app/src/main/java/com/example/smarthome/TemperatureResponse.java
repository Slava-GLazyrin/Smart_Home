package com.example.test;

import com.google.gson.annotations.SerializedName;

public class TemperatureResponse {
    
    @SerializedName("temperature")
    private Integer temperature;
    
    @SerializedName("unit")
    private String unit;
    
    @SerializedName("timestamp")
    private Double timestamp;
    
    @SerializedName("status")
    private String status;
    
    // Конструктор по умолчанию (нужен для GSON)
    public TemperatureResponse() {
    }
    
    // Безопасный геттер для температуры
    public Integer getTemperature() {
        if (temperature == null) {
            return 0; // Значение по умолчанию
        }
        
        // Проверка диапазона 
        if (temperature < -50 || temperature > 60) {
            return 0;
        }
        return temperature;
    }
    
    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }
    
    // Геттер с проверкой для UI
    public String getFormattedTemperature() {
        Integer temp = getTemperature();
        return temp + "°C";
    }
    
    // Геттеры для остальных полей с проверкой на null
    public String getUnit() {
        return unit != null ? unit : "celsius";
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public Double getTimestamp() {
        return timestamp != null ? timestamp : 0.0;
    }
    
    public void setTimestamp(Double timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getStatus() {
        return status != null ? status : "unknown";
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    // Проверка валидности ответа
    public boolean isValid() {
        return temperature != null && "success".equals(status);
    }
    
    @Override
    public String toString() {
        return "TemperatureResponse{" +
                "temperature=" + temperature +
                ", unit='" + unit + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                '}';
    }
}