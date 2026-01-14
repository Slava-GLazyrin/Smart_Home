package com.example.test;

import android.os.Bundle;
import android.view.View;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Button;
import android.widget.Switch;
import android.widget.SeekBar;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private TextView textTemperature;
    private TextView tvTargetTemp; // Добавляем для отображения целевой температуры
    private TextView tvStatus;     // Для статусных сообщений
    private ProgressBar progressBar;
    private boolean isLoading = false;
    private EditText editTemperature;
    private Button buttonAirConditioner;
    private Button buttonUpdate;
    private Button btnSetTemp;     // Кнопка установки температуры
    private SeekBar sbTargetTemp;  // SeekBar для выбора температуры
    private Switch switchAC;       // Переключатель кондиционера
    
    private boolean isAirConditionerOn = false;
    private int currentTemperature = Constants.DEFAULT_TEMPERATURE;
    private int targetTemperature = Constants.DEFAULT_TEMPERATURE;
    
    private TemperatureService temperatureService;
    private Call<TemperatureResponse> currentCall;
    private Call<ApiResponse> targetTempCall;
    private Call<ApiResponse> acCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация всех View элементов
        progressBar = findViewById(R.id.progressBar);
        textTemperature = findViewById(R.id.text_temperature);
        editTemperature = findViewById(R.id.edit_temperature);
        buttonAirConditioner = findViewById(R.id.button_air_conditioner);
        buttonUpdate = findViewById(R.id.button_update);
        tvTargetTemp = findViewById(R.id.tv_target_temp);
        tvStatus = findViewById(R.id.tv_status);
        btnSetTemp = findViewById(R.id.btn_set_temp);
        sbTargetTemp = findViewById(R.id.sb_target_temp);
        switchAC = findViewById(R.id.switch_ac);
        
        // Настройка SeekBar с константами
        sbTargetTemp.setMax(Constants.MAX_TEMPERATURE - Constants.MIN_TEMPERATURE);
        sbTargetTemp.setProgress(Constants.DEFAULT_TEMPERATURE - Constants.MIN_TEMPERATURE);
        tvTargetTemp.setText(Constants.DEFAULT_TEMPERATURE + "°C");
        
        // Отображаем начальную температуру
        textTemperature.setText("Текущая температура: " + currentTemperature + "°C");

        // Создаем клиент Retrofit
        Retrofit retrofit = RetrofitClient.getClient(Constants.BASE_URL);
        temperatureService = retrofit.create(TemperatureService.class);
        
        // Загружаем начальную температуру
        fetchTemperature();

        // Обработчик нажатия на кнопку "Обновить"
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    Toast.makeText(MainActivity.this, Constants.MSG_AC_OFF, Toast.LENGTH_SHORT).show();
                } else {
                    // Если кондиционер выключен, включаем его с установкой целевой температуры
                    String desiredTempStr = editTemperature.getText().toString();

                    // Ограничиваем длину ввода (используем константу)
                    if (desiredTempStr.length() > Constants.MAX_TEMP_LENGTH) {
                        Toast.makeText(MainActivity.this, 
                            "Температура не может быть более " + Constants.MAX_TEMP_LENGTH + " символов", 
                            Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (desiredTempStr.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Введите желаемую температуру", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        int desiredTemp = Integer.parseInt(desiredTempStr);
                        
                        // Проверяем диапазон с помощью констант
                        if (desiredTemp < Constants.MIN_TEMPERATURE || desiredTemp > Constants.MAX_TEMPERATURE) {
                            Toast.makeText(MainActivity.this, 
                                "Температура должна быть от " + Constants.MIN_TEMPERATURE + 
                                " до " + Constants.MAX_TEMPERATURE + "°C", 
                                Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        setTargetTemperature(desiredTemp);
                        updateAirConditionerStatus(true);
                        Toast.makeText(MainActivity.this, 
                            Constants.MSG_AC_ON + " и установлен на " + desiredTemp + "°C", 
                            Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "Введите корректное значение температуры", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        
        // Обработчик для кнопки установки температуры
        btnSetTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String desiredTempStr = editTemperature.getText().toString();
                if (desiredTempStr.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Введите температуру", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                try {
                    int desiredTemp = Integer.parseInt(desiredTempStr);
                    setTargetTemperature(desiredTemp);
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Введите число", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        // Обработчик для SeekBar
        sbTargetTemp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    targetTemperature = progress + Constants.MIN_TEMPERATURE;
                    tvTargetTemp.setText(targetTemperature + "°C");
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Автоматически устанавливаем температуру при отпускании
                setTargetTemperature(targetTemperature);
            }
        });
        
        // Обработчик для переключателя кондиционера
        switchAC.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateAirConditionerStatus(isChecked);
            Toast.makeText(MainActivity.this, 
                isChecked ? Constants.MSG_AC_ON : Constants.MSG_AC_OFF, 
                Toast.LENGTH_SHORT).show();
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
        switchAC.setChecked(isAirConditionerOn);
    }
    
    // Метод для показа/скрытия загрузки
    private void showLoading(boolean show) {
        isLoading = show;
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        
        // Блокируем кнопки во время загрузки
        buttonUpdate.setEnabled(!show);
        btnSetTemp.setEnabled(!show);
        buttonAirConditioner.setEnabled(!show);
        switchAC.setEnabled(!show);
        sbTargetTemp.setEnabled(!show);
        editTemperature.setEnabled(!show);
    }
    
    // Метод для показа статуса
    private void showStatus(String status) {
        tvStatus.setText(status);
    }
    
    // Метод для показа ошибки
    private void showError(String error) {
        showLoading(false);
        tvStatus.setText("Ошибка: " + error);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    // Метод для получения температуры с сервера
    private void fetchTemperature() {
        if (!isNetworkAvailable()) {
            showError(Constants.MSG_NO_INTERNET);
            return;
        }
        
        if (isLoading) {
            return; // Уже загружается
        }
        
        showLoading(true);
        showStatus(Constants.MSG_LOADING);
        
        // Отменяем предыдущий запрос, если он есть
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
        
        currentCall = temperatureService.getTemperature();
        currentCall.enqueue(new Callback<TemperatureResponse>() {
            @Override
            public void onResponse(Call<TemperatureResponse> call, Response<TemperatureResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    TemperatureResponse tempResponse = response.body();
                    if (tempResponse != null && tempResponse.getTemperature() != null) {
                        currentTemperature = tempResponse.getTemperature();
                        updateCurrentTemperature();
                        showStatus("Температура обновлена: " + currentTemperature + "°C");
                    } else {
                        showError("Неверный ответ от сервера");
                    }
                } else {
                    showError("Ошибка сервера: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TemperatureResponse> call, Throwable t) {
                if (!call.isCanceled()) {
                    showLoading(false);
                    showError("Ошибка соединения: " + t.getMessage());
                }
            }
        });
    }

    // Метод для установки желаемой температуры на сервере
    private void setTargetTemperature(int targetTemperature) {
        if (!isNetworkAvailable()) {
            showError(Constants.MSG_NO_INTERNET);
            return;
        }
        
        // Проверяем диапазон с помощью констант
        if (targetTemperature < Constants.MIN_TEMPERATURE || 
            targetTemperature > Constants.MAX_TEMPERATURE) {
            showError("Температура должна быть от " + Constants.MIN_TEMPERATURE + 
                     " до " + Constants.MAX_TEMPERATURE + "°C");
            return;
        }
        
        showLoading(true);
        showStatus("Устанавливаю температуру...");
        
        // Отменяем предыдущий запрос, если он есть
        if (targetTempCall != null && !targetTempCall.isCanceled()) {
            targetTempCall.cancel();
        }
        
        TargetTemperatureRequest request = new TargetTemperatureRequest(targetTemperature);
        targetTempCall = temperatureService.setTargetTemperature(request);
        targetTempCall.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if ("success".equals(apiResponse.getStatus())) {
                        showStatus(apiResponse.getMessage());
                        Toast.makeText(MainActivity.this, 
                            "Целевая температура: " + targetTemperature + "°C", 
                            Toast.LENGTH_SHORT).show();
                        // Обновляем SeekBar
                        sbTargetTemp.setProgress(targetTemperature - Constants.MIN_TEMPERATURE);
                    } else {
                        showError("Сервер вернул ошибку: " + apiResponse.getMessage());
                    }
                } else {
                    showError("Не удалось установить температуру: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (!call.isCanceled()) {
                    showLoading(false);
                    showError("Ошибка соединения: " + t.getMessage());
                }
            }
        });
    }

    // Метод для включения/выключения кондиционера
    private void updateAirConditionerStatus(boolean state) {
        if (!isNetworkAvailable()) {
            showError(Constants.MSG_NO_INTERNET);
            return;
        }
        
        showLoading(true);
        showStatus(state ? "Включаю кондиционер..." : "Выключаю кондиционер...");
        
        // Отменяем предыдущий запрос, если он есть
        if (acCall != null && !acCall.isCanceled()) {
            acCall.cancel();
        }
        
        acCall = temperatureService.toggleAirConditioner(new AirConditionerRequest(state));
        acCall.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if ("success".equals(apiResponse.getStatus())) {
                        isAirConditionerOn = state;
                        updateAirConditionerButton();
                        showStatus(apiResponse.getMessage());
                    } else {
                        showError("Сервер вернул ошибку: " + apiResponse.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (!call.isCanceled()) {
                    showLoading(false);
                    showError("Ошибка соединения: " + t.getMessage());
                }
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Отменяем все pending запросы
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
        if (targetTempCall != null && !targetTempCall.isCanceled()) {
            targetTempCall.cancel();
        }
        if (acCall != null && !acCall.isCanceled()) {
            acCall.cancel();
        }
    }
}