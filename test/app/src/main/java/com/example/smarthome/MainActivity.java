package com.example.test;

import android.os.Bundle;
import android.view.View;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.widget.Button;
import android.widget.Switch;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private TextView textTemperature;
    private EditText editTemperature;
    private Button buttonAirConditioner;
    private Button buttonUpdate;
    private boolean isAirConditionerOn = false;  // Флаг, указывающий, включен ли кондиционер
    private int currentTemperature = 25;  // Текущая температура, начальное значение 25°C

    private TemperatureService temperatureService; // Сервис для общения с API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textTemperature = findViewById(R.id.text_temperature);
        editTemperature = findViewById(R.id.edit_temperature);
        buttonAirConditioner = findViewById(R.id.button_air_conditioner);
        buttonUpdate = findViewById(R.id.button_update);

        // Создаем клиент Retrofit
        Retrofit retrofit = RetrofitClient.getClient("http://10.0.2.2:5000/");  // Указываем базовый URL вашего сервера

        // Инициализируем сервис для получения температуры
        temperatureService = retrofit.create(TemperatureService.class);
        fetchTemperature();

        // Обработчик нажатия на кнопку "Обновить"
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Запрос на сервер для получения текущей температуры
                fetchTemperature();
            }
        });

        // Обработчик нажатия на кнопку кондиционера
        buttonAirConditioner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAirConditionerOn) {
                    // Если кондиционер включён, выключаем его
                    updateAirConditionerStatus(false);
                    Toast.makeText(MainActivity.this, "Кондиционер выключен", Toast.LENGTH_SHORT).show();
                } else {
                    // Если кондиционер выключен, включаем его с установкой целевой температуры
                    String desiredTempStr = editTemperature.getText().toString();

                    // Ограничиваем длину ввода (например, 3 символа)
                    if (desiredTempStr.length() > 3) {
                        Toast.makeText(MainActivity.this, "Температура не может быть более 3 символов", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (desiredTempStr.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Введите желаемую температуру", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        int desiredTemp = Integer.parseInt(desiredTempStr);
                        setTargetTemperature(desiredTemp);
                        updateAirConditionerStatus(true);
                        Toast.makeText(MainActivity.this, "Кондиционер включён и установлен на " + desiredTemp + "°C", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "Введите корректное значение температуры", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        

    }

    // Метод для обновления текущей температуры
    private void updateCurrentTemperature() {
        textTemperature.setText("Текущая температура: " + currentTemperature + "°C");
    }

    // Метод для обновления состояния кнопки кондиционера
    private void updateAirConditionerButton() {
        if (isAirConditionerOn) {
            buttonAirConditioner.setText("Выключить кондиционер");
        } else {
            buttonAirConditioner.setText("Включить кондиционер");
        }
    }

    // Метод для получения температуры с сервера
    private void fetchTemperature() {
        Call<TemperatureResponse> call = temperatureService.getTemperature();
        call.enqueue(new Callback<TemperatureResponse>() {
            @Override
            public void onResponse(Call<TemperatureResponse> call, Response<TemperatureResponse> response) {
                if (response.isSuccessful()) {
                    TemperatureResponse tempResponse = response.body();
                    if (tempResponse != null) {
                        currentTemperature = tempResponse.getTemperature();
                        updateCurrentTemperature();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Ошибка на сервере: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TemperatureResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Ошибка соединения", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Метод для установки желаемой температуры на сервере
    private void setTargetTemperature(int targetTemperature) {
        TargetTemperatureRequest request = new TargetTemperatureRequest(targetTemperature);
        Call<Void> call = temperatureService.setTargetTemperature(request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Температура успешно обновлена на сервере
                    Toast.makeText(MainActivity.this, "Температура установлена", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Ошибка при установке температуры", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Ошибка соединения", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Метод для включения/выключения кондиционера
    private void updateAirConditionerStatus(boolean state) {
        Call<Void> call = temperatureService.toggleAirConditioner(new AirConditionerRequest(state));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    isAirConditionerOn = state;
                    updateAirConditionerButton();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Ошибка соединения", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
    
    private void fetchCurrentTemperature() {
        if (!isNetworkAvailable()) {
            showError("No internet connection");
            tvStatus.setText("Error: No internet connection");
            return;
        }
        
        showStatus("Fetching temperature...");
        
        Call<TemperatureResponse> call = TemperatureService.getApiService().getCurrentTemperature();
        call.enqueue(new Callback<TemperatureResponse>() {
            @Override
            public void onResponse(Call<TemperatureResponse> call, Response<TemperatureResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TemperatureResponse tempResponse = response.body();
                    if (tempResponse != null && tempResponse.getTemperature() != null) {
                        int temp = tempResponse.getTemperature();
                        tvCurrentTemp.setText(temp + "°C");
                        showStatus("Temperature updated: " + temp + "°C");
                    } else {
                        showError("Invalid response from server");
                    }
                } else {
                    showError("Server error: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<TemperatureResponse> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void setTargetTemperature(int temperature) {
        if (!isNetworkAvailable()) {
            showError("No internet connection");
            return;
        }
        
        showStatus("Setting target temperature...");
        
        TargetTemperatureRequest request = new TargetTemperatureRequest(temperature);
        Call<ApiResponse> call = TemperatureService.getApiService().setTargetTemperature(request);
        
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if ("success".equals(apiResponse.getStatus())) {
                        showStatus(apiResponse.getMessage());
                        Toast.makeText(MainActivity.this, 
                            "Target set to " + temperature + "°C", 
                            Toast.LENGTH_SHORT).show();
                    } else {
                        showError("Server returned error: " + apiResponse.getMessage());
                    }
                } else {
                    showError("Failed to set temperature: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }
}
