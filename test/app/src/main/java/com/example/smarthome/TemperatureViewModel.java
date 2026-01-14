package com.example.test;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TemperatureViewModel extends ViewModel {
    
    private MutableLiveData<Integer> currentTemperature = new MutableLiveData<>(0);
    private MutableLiveData<Integer> targetTemperature = new MutableLiveData<>(25);
    private MutableLiveData<Boolean> airConditionerState = new MutableLiveData<>(false);
    private MutableLiveData<String> statusMessage = new MutableLiveData<>("");
    
    public LiveData<Integer> getCurrentTemperature() {
        return currentTemperature;
    }
    
    public LiveData<Integer> getTargetTemperature() {
        return targetTemperature;
    }
    
    public LiveData<Boolean> getAirConditionerState() {
        return airConditionerState;
    }
    
    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }
    
    public void setCurrentTemperature(int temperature) {
        currentTemperature.setValue(temperature);
    }
    
    public void setTargetTemperature(int temperature) {
        targetTemperature.setValue(temperature);
    }
    
    public void setAirConditionerState(boolean state) {
        airConditionerState.setValue(state);
    }
    
    public void setStatusMessage(String message) {
        statusMessage.setValue(message);
    }
}