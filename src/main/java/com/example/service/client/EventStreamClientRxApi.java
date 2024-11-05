package com.example.service.client;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class EventStreamClientRxApi {

    public static final String FLOWABLE_ENDPOINT = "flowable";
    public static final String BACKPRESSURE_ENDPOINT = "backpressure";
    public static final String ERROR_HANDLING_ENDPOINT = "errorHandling";
    public static final String ERRHANDLING_ONERRORRETURN = "onErrorReturn";
    public static final String ERRHANDLING_DOONERROR = "doOnError";
    public static final String ERRHANDLING_ONERRORRESUMENEXT = "onErrorResumeNext";
    private final OkHttpClient client;
    private final String providerUrl;

    public EventStreamClientRxApi(@Value("${provider.service.retrofit.url}") String providerUrl) {
        this.providerUrl = providerUrl;
        this.client = new OkHttpClient();
    }

    public Flowable<String> getFastSensorUpdates() {
        HttpUrl urlWithParams = HttpUrl.parse(providerUrl + FLOWABLE_ENDPOINT)
                .newBuilder()
                .build();
        return streamEvents(urlWithParams, BackpressureStrategy.BUFFER);
    }

    public Flowable<String> getBackpressureFlowable(
            BackpressureStrategy backpressureStrategy, boolean isFastProducer, int dataSize) {
        HttpUrl urlWithParams = HttpUrl.parse(providerUrl + BACKPRESSURE_ENDPOINT)
                .newBuilder()
                .addQueryParameter("backpressureStrategy", backpressureStrategy.name())
                .addQueryParameter("isFastProducer", String.valueOf(isFastProducer))
                .addQueryParameter("dataSize", String.valueOf(dataSize))
                .build();
        return streamEvents(urlWithParams, backpressureStrategy);
    }

    public Flowable<String> getErrorHandlingFlowable(String errorHandlingFlag) {
        HttpUrl urlWithParams = HttpUrl.parse(providerUrl + ERROR_HANDLING_ENDPOINT)
                .newBuilder()
                .addQueryParameter("errorHandlingFlag", errorHandlingFlag)
                .build();
        return streamEvents(urlWithParams, BackpressureStrategy.BUFFER);
    }

    public Flowable<String> streamEvents(HttpUrl url, BackpressureStrategy backpressureStrategy) {
        return Flowable.create(emitter -> {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            // asynchronously make the call
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    emitter.onError(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        emitter.onError(new IOException("Unexpected code " + response));
                        return;
                    }

                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            emitter.onNext(line);
                        }
                        emitter.onComplete();
                    } else {
                        emitter.onError(new IOException("Response body is null"));
                    }
                }
            });
        }, backpressureStrategy);
    }
}
