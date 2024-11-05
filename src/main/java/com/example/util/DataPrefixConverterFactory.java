package com.example.util;

import com.example.model.SensorData;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class DataPrefixConverterFactory extends Converter.Factory {

    private final ObjectMapper objectMapper;

    // Constructor to accept ObjectMapper as a bean
    public DataPrefixConverterFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Converter<ResponseBody, SensorData> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        // Create your own converter that handles the data prefix
        return new Converter<ResponseBody, SensorData>() {
            @Override
            public SensorData convert(ResponseBody value) throws IOException {
                // Read the response and remove the prefix
                String bodyString = value.string();
                if (bodyString.startsWith("data:")) {
                    bodyString = bodyString.substring("data:".length());
                }
                // Convert the cleaned string to the desired object
                return objectMapper.readValue(bodyString, SensorData.class);
            }
        };
    }
}
