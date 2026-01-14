package com.example.test;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;
import java.io.IOException;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(String baseUrl) {
        if (retrofit == null) {
            // Логирование запросов и ответов
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(Constants.LOG_LEVEL);

            // Добавляем Interceptor для проверки размера запроса
            Interceptor requestSizeInterceptor = chain -> {
                Request request = chain.request();

                // Проверка размера тела запроса (если оно существует)
                if (request.body() != null) {
                    okio.Buffer buffer = new okio.Buffer();
                    request.body().writeTo(buffer);
                    long requestSize = buffer.size();

                    if (requestSize > Constants.MAX_REQUEST_SIZE) {
                        throw new IOException(Constants.ERROR_MSG_REQUEST_TOO_LARGE + 
                                Constants.MAX_REQUEST_SIZE + " байт"
                        );
                    }
                }catch (IOException e) {
                        throw new IOException("Failed to check request size: " + e.getMessage());
                    }
                }

                return chain.proceed(request);
            };

            // Добавляем Interceptor для заголовков
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request originalRequest = chain.request();
                        Request requestWithHeaders = originalRequest.newBuilder()
                                .addHeader("Accept", Constants.HEADER_ACCEPT) // Указываем тип контента
                                .addHeader("Content-Type", Constants.HEADER_CONTENT_TYPE);
                                .Request requestWithHeaders = requestBuilder.build();
                        return chain.proceed(requestWithHeaders);
                    })
                    .addInterceptor(requestSizeInterceptor) // Проверка размера запроса
                    .addInterceptor(loggingInterceptor) // Подключаем логирование
                    .build();

            // Инициализация Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
     //Валидация и нормализация URL
     
    private static String validateBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            // Используем URL по умолчанию из констант
            return Constants.BASE_URL;
        }
        
        // Убедимся, что URL заканчивается на "/"
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        
        return baseUrl;
    }
    //Метод для получения сервиса напрямую (удобный shortcut)
    public static TemperatureService getTemperatureService() {
        return getClient(Constants.BASE_URL).create(TemperatureService.class);
    }
}

