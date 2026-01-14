package com.example.test;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

// Интерфейс TemperatureService для общения с API
public interface TemperatureService {
    
    // Константы для эндпоинтов (можно вынести в Constants.java)
    String ENDPOINT_TEMPERATURE = "/temperature";
    String ENDPOINT_SET_TARGET = "/set_target_temperature";
    String ENDPOINT_AIR_CONDITIONER = "/air_conditioner";
    
    @GET(ENDPOINT_TEMPERATURE)
    Call<TemperatureResponse> getTemperature();
    
    @POST(ENDPOINT_SET_TARGET)
    Call<ApiResponse> setTargetTemperature(@Body TargetTemperatureRequest request);
    
    @POST(ENDPOINT_AIR_CONDITIONER)
    Call<ApiResponse> toggleAirConditioner(@Body AirConditionerRequest request);
}


