package com.example.test;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("target_temperature")
    private Integer targetTemperature;
    
    @SerializedName("air_conditioner")
    private String airConditioner;
    
    @SerializedName("air_conditioner_state")
    private boolean airConditionerState;
    
    public String getStatus() {
        return status != null ? status : "error";
    }
    
    public String getMessage() {
        return message != null ? message : "No message";
    }
    
    public Integer getTargetTemperature() {
        return targetTemperature;
    }
    
    public String getAirConditioner() {
        return airConditioner;
    }
    
    public boolean getAirConditionerState() {
        return airConditionerState;
    }
    
    public boolean isSuccess() {
        return "success".equals(status);
    }
}