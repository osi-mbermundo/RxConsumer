package com.example.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Flowable;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.ArrayList;

public class SseConverterFactory extends Converter.Factory {
    private final ObjectMapper objectMapper;

    public SseConverterFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public static SseConverterFactory create(ObjectMapper objectMapper) {
        return new SseConverterFactory(objectMapper);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new SseConverter<>(objectMapper, type);
    }

    private static class SseConverter<T> implements Converter<ResponseBody, Flowable<T>> {
        private final ObjectMapper objectMapper;
        private final Type type;

        SseConverter(ObjectMapper objectMapper, Type type) {
            this.objectMapper = objectMapper;
            this.type = type;
        }

        @Override
        public Flowable<T> convert(ResponseBody value) throws IOException {
            return Flowable.create(emitter -> {
                try (BufferedReader reader = new BufferedReader(value.charStream())) {
                    String line;
                    // Read each line and parse lines that start with "data:"
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data:")) {
                            String json = line.substring(5).trim();  // Remove "data:" prefix
                            T data = objectMapper.readValue(json, objectMapper.constructType(type));
                            emitter.onNext(data);  // Emit each parsed object
                        }
                    }
                    emitter.onComplete();  // Complete the flowable when done
                } catch (Exception e) {
                    emitter.onError(e);  // Emit an error if something goes wrong
                }
            }, io.reactivex.rxjava3.core.BackpressureStrategy.BUFFER);
        }
    }
}