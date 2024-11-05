package com.example.service;

import com.example.model.SensorData;
import com.example.service.client.RetrofitClientRxApi;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Retrofit;

@Service
public class RetrofitClientRxService {

    private static final Logger logger = LoggerFactory.getLogger(RetrofitClientRxService.class);

    private final RetrofitClientRxApi clienRxApi;

    @Autowired
    public RetrofitClientRxService(Retrofit retrofit) {
        this.clienRxApi = retrofit.create(RetrofitClientRxApi.class);
    }


    public Observable<SensorData> consumeObservable() {
        logger.info("[Observable] Starting to consume observable sensor data...");
        return clienRxApi.getSensorUpdates()
                .publish()
                .refCount() // This manage the subscription
                .doOnSubscribe(subscription -> logger.info("[Observable] Subscription started."))
                .doOnNext(data -> logger.info("[Observable] Received sensor data: {}", data))
                .doOnError(throwable -> logger.error("[Observable] Error occurred while fetching sensor data: {}", throwable.getMessage()))
                .doOnComplete(() -> logger.info("[Observable] Completed successfully."));
    }

    public Flowable<SensorData> consumeFlowable() {
        logger.info("[Flowable] Starting to consume flowable sensor data...");
        return clienRxApi.getFastSensorUpdates()
                .publish()
                .refCount() // This manage the subscription
                .doOnSubscribe(subscription -> logger.info("[Flowable] Subscription started."))
                .doOnNext(data -> logger.info("[Flowable] Received sensor data: {}", data))
                .doOnError(throwable -> logger.error("[Flowable] Error occurred while fetching sensor data: {}", throwable.getMessage()))
                .doOnComplete(() -> logger.info("[Flowable] Completed successfully."))
                .doOnCancel(() -> logger.info("[Flowable] Subscription cancelled"));
    }


    public Single<SensorData> consumeSingle() {
        logger.info("[Single] Starting to consume single sensor data...");
        return clienRxApi.getSingleSensorUpdate()
                .doOnSubscribe(subscription -> logger.info("[Single] Subscription started."))
                .doOnSuccess(data -> logger.info("[Single] Received sensor data: {}", data))
                .doFinally(() -> logger.info("[Single] Completed successfully."))
                .doOnError(throwable -> logger.error("[Single] Error occurred while fetching sensor data: {}", throwable.getMessage()));
    }

    public Maybe<SensorData> consumeMaybe(boolean flag) {
        logger.info("[Maybe] Starting to consume maybe sensor data... flag: {}", flag);
        return clienRxApi.getMaybeSensorUpdate(flag)
                .doOnSubscribe(subscription -> logger.info("[Maybe] Subscription started."))
                .doOnSuccess(data -> logger.info("[Maybe] Received sensor data: {}", data))
                .doFinally(() -> logger.info("[Maybe] Completed successfully."))
                .doOnError(throwable -> logger.error("[Maybe] Error occurred while fetching sensor data: {}", throwable.getMessage()));
    }

    public Completable consumeCompletable() {
        logger.info("[Completable] Starting to consume completable process...");
        return clienRxApi.calibrateSensors()
                .doOnSubscribe(subscription -> logger.info("[Completable] Subscription started."))
                .doOnError(throwable -> logger.error("[Completable] Error occurred while processing: {}", throwable.getMessage()))
                .doOnComplete(() -> logger.info("[Completable] Completed successfully."));
    }

    public Flowable<SensorData> consumeBackpressure(String backpressureStrategy, boolean isFastProducer) {
        logger.info("[Backpressure {}] Starting to consume flowable sensor data...", backpressureStrategy);
        return clienRxApi.getBackpressure(backpressureStrategy, isFastProducer)
                .publish()
                .refCount() // This manage the subscription
                .doOnSubscribe(
                        subscription -> logger.info("[Backpressure {}] Subscription started.", backpressureStrategy))
                .doOnNext(
                        data -> logger.info("[Backpressure {}] Received sensor data: {}", backpressureStrategy, data))
                .doOnError(
                        throwable -> logger.error("[Backpressure {}] Error occurred while fetching sensor data: {}",
                                backpressureStrategy, throwable.getMessage()))
                .doOnCancel(() -> logger.info("[Backpressure {}] Subscription cancelled", backpressureStrategy))
                .doOnComplete(() -> logger.info("[Backpressure {}] Completed successfully.", backpressureStrategy))
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.io());
    }

    public @NonNull Disposable fetchSensorData(String backpressureStrategy, boolean isFastProducer
            , SensorDataCallback callback) {


        return clienRxApi.getBackpressure(backpressureStrategy, isFastProducer)
                .subscribeOn(Schedulers.io()) // Run on background thread
                .observeOn(Schedulers.computation()) // Observe on main thread
                .subscribe(
                        sensorData -> {
                            logger.info("Sensor Data: " + sensorData);
                            callback.onSuccess(sensorData);
                        },
                        throwable -> {
                            logger.error("Error occurred: " + throwable.getMessage());
                            callback.onError(throwable);
                        }
                );
    }

    public interface SensorDataCallback {
        void onSuccess(SensorData sensorData);

        void onError(Throwable throwable);
    }
}
