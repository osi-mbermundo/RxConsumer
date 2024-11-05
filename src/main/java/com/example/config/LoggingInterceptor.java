package com.example.config;

import okhttp3.Interceptor;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoggingInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public Response intercept(Chain chain) throws IOException {
        // Log the request
        okhttp3.Request request = chain.request();
        logger.info("Sending request to URL: {}", request.url());
        logger.info("Request method: {}", request.method());
        logger.info("Request headers: {}", request.headers());

        // Proceed with the request
        Response response = chain.proceed(request);

        // Log the response
        logger.info("Received response for URL: {}", response.request().url());
        logger.info("Response code: {}", response.code());
        logger.info("Response headers: {}", response.headers());
        // Process response body without blocking
        if (response.body() != null) {
            // For example, you can use a buffer to read without blocking completely
            BufferedSource source = response.body().source();
            source.request(Long.MAX_VALUE); // Request the entire body
            Buffer buffer = source.getBuffer();
            logger.info("Response body: {}", buffer.clone().readUtf8()); // Read without consuming the original body
        } else {
            logger.info("Response body: null");
        }

        return response;
    }
}
