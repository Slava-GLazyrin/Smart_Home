package com.example.test;

public class Constants {
    
    // Температурные константы
    public static final int MIN_TEMPERATURE = 16;
    public static final int MAX_TEMPERATURE = 40;
    public static final int DEFAULT_TEMPERATURE = 25;
    public static final int MAX_TEMP_LENGTH = 3;
    
    // Сетевые константы для Retrofit
    public static final String BASE_URL = "http://10.0.2.2:5000/";
    public static final long MAX_REQUEST_SIZE = 1024 * 1024; // 1 MB в байтах
    public static final int CONNECT_TIMEOUT_SECONDS = 30;
    public static final int READ_TIMEOUT_SECONDS = 30;
    public static final int WRITE_TIMEOUT_SECONDS = 30;
    
    // Заголовки
    public static final String HEADER_ACCEPT = "application/json";
    public static final String HEADER_CONTENT_TYPE = "application/json";
    
    // Уровни логирования
    public static final HttpLoggingInterceptor.Level LOG_LEVEL = HttpLoggingInterceptor.Level.BODY;
    
    // Коды ошибок
    public static final int ERROR_REQUEST_TOO_LARGE = 413;
    public static final int ERROR_NO_NETWORK = 1001;
    public static final int ERROR_SERVER = 1002;
    
    // Сообщения об ошибках
    public static final String ERROR_MSG_REQUEST_TOO_LARGE = "Request size exceeds limit: ";
    
    // SharedPreferences
    public static final String PREFS_NAME = "SmartHomePrefs";
    public static final String KEY_LAST_TEMP = "last_temperature";
    public static final String KEY_AC_STATE = "air_conditioner_state";
}