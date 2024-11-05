package com.example.service.client;

import com.example.model.SensorData;
import com.example.model.SensorResponse;
import io.reactivex.rxjava3.core.*;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface RetrofitClientRxApi {

    @GET("observable")
    @Streaming
    Observable<SensorData> getSensorUpdates();

    @GET("flowable")
    @Streaming
    Flowable<SensorData> getFastSensorUpdates();

    @GET("single")
    @Streaming
    Single<SensorData> getSingleSensorUpdate();

    @GET("maybe")
    @Streaming
    Maybe<SensorData> getMaybeSensorUpdate(@Query("flag") boolean flag);

    @POST("calibrate")
    @Streaming
    Completable calibrateSensors();

    @GET("backpressure")
    @Streaming
    Flowable<SensorData> getBackpressure(
            @Query("backpressureStrategy") String backpressureStrategy,
            @Query("isFastProducer") boolean isFastProducer);
}
