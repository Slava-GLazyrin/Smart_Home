package com.example.test;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

// Интерфейс TemperatureService для общения с API
public interface TemperatureService {

    @GET("/temperature")
    Call<TemperatureResponse> getTemperature();

    @POST("/set_target_temperature")
    Call<Void> setTargetTemperature(@Body TargetTemperatureRequest request);

    @POST("/air_conditioner")
    Call<Void> toggleAirConditioner(@Body AirConditionerRequest request);
}


