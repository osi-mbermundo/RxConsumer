package com.example.config;

import com.example.util.DataPrefixConverterFactory;
import com.example.util.LocalDateTimeAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

import java.time.LocalDateTime;

@Configuration
public class RetrofitConfig {

    private final String providerUrl;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Register the module for Java time types
        return objectMapper;
    }

    public RetrofitConfig(@Value("${provider.service.retrofit.url}") String providerUrl) {
        this.providerUrl = providerUrl;
    }

    @Bean
    public Retrofit retrofit(ObjectMapper objectMapper) {
//    public Retrofit retrofit() {
//        val loggingInterceptor = HttpLoggingInterceptor();
//        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        OkHttpClient client = new OkHttpClient.Builder()
//                .addInterceptor(new DataPrefixInterceptor())
//                .addInterceptor(chain -> {
//                    Request request = chain.request();
//                    Response response = chain.proceed(request);
//                    // Log the response
//                    return response;
//                })
                .build();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setLenient()
                .create();

        return new Retrofit.Builder()
                .baseUrl(providerUrl)
                .client(client)
//                .addConverterFactory(SseConverterFactory.create(objectMapper))
//                .addConverterFactory(JacksonConverterFactory.create())
//                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(new DataPrefixConverterFactory(objectMapper))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
    }
}
