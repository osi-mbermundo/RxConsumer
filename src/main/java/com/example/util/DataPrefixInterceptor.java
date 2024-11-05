package com.example.util;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

import java.io.IOException;

public class DataPrefixInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());

        // Get the original response body
        ResponseBody originalBody = response.body();
        if (originalBody == null) {
            return response; // Return the response as-is if the body is null
        }

        // Read the response body into a Buffer
        Buffer buffer = new Buffer();
        buffer.write(originalBody.bytes()); // Read the body bytes to the buffer

        String responseBodyString = buffer.readUtf8(); // Read the body from the buffer

        // Check for and remove the 'data: ' prefix if present
        if (responseBodyString.startsWith("data:")) {
            responseBodyString = responseBodyString.substring("data:".length());
        }

        // Create a new ResponseBody from the modified string
        ResponseBody modifiedBody = ResponseBody.create(originalBody.contentType(), responseBodyString);

        // Return a new response with the modified body
        return response.newBuilder()
                .body(modifiedBody)
                .build();
    }
}
