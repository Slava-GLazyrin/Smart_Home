package com.example.test;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(String baseUrl) {
        if (retrofit == null) {
            // Логирование запросов и ответов
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Добавляем Interceptor для проверки размера запроса
            Interceptor requestSizeInterceptor = chain -> {
                Request request = chain.request();

                // Проверка размера тела запроса (если оно существует)
                if (request.body() != null) {
                    okio.Buffer buffer = new okio.Buffer();
                    request.body().writeTo(buffer);
                    long requestSize = buffer.size();

                    if (requestSize > MAX_REQUEST_SIZE) {
                        throw new IllegalArgumentException("Размер запроса превышает допустимый предел: " + MAX_REQUEST_SIZE + " байт");
                    }
                }

                return chain.proceed(request);
            };

            // Добавляем Interceptor для заголовков
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request originalRequest = chain.request();
                        Request requestWithHeaders = originalRequest.newBuilder()
                                .addHeader("Accept", "application/json") // Указываем тип контента
                                .build();
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
}

