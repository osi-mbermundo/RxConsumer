package com.example.service;

import com.example.model.SensorData;
import com.example.service.client.WebClientRxService;
import io.reactivex.rxjava3.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerRxService {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerRxService.class);
    private final WebClientRxService webClient;

    public ConsumerRxService(WebClientRxService webClient) {
        this.webClient = webClient;
    }

    public Observable<SensorData> consumeObservable() {
        logger.info("[Observable] Starting to consume observable sensor data...");
        return webClient.getSensorUpdates()
                .publish()
                .refCount() // This manage the subscription
                .doOnSubscribe(subscription -> logger.info("[Observable] Subscription started."))
                .doOnNext(data -> logger.info("[Observable] Received sensor data: {}", data))
                .doOnError(throwable -> logger.error("[Observable] Error occurred while fetching sensor data: {}", throwable.getMessage()))
                .doOnComplete(() -> logger.info("[Observable] Completed successfully."));
    }

    public Flowable<SensorData> consumeFlowable() {
        logger.info("[Flowable] Starting to consume flowable sensor data...");
        return webClient.getFastSensorUpdates()
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
        return webClient.getSingleSensorUpdate()
                .doOnSubscribe(subscription -> logger.info("[Single] Subscription started."))
                .doOnSuccess(data -> logger.info("[Single] Received sensor data: {}", data))
                .doFinally(() -> logger.info("[Single] Completed successfully."))
                .doOnError(throwable -> logger.error("[Single] Error occurred while fetching sensor data: {}", throwable.getMessage()));
    }

    public Maybe<SensorData> consumeMaybe(boolean flag) {
        logger.info("[Maybe] Starting to consume maybe sensor data... flag: {}", flag);
        return webClient.getMaybeSensorUpdate(flag)
                .doOnSubscribe(subscription -> logger.info("[Maybe] Subscription started."))
                .doOnSuccess(data -> logger.info("[Maybe] Received sensor data: {}", data))
                .doFinally(() -> logger.info("[Maybe] Completed successfully."))
                .doOnError(throwable -> logger.error("[Maybe] Error occurred while fetching sensor data: {}", throwable.getMessage()));
    }

    public Completable consumeCompletable() {
        logger.info("[Completable] Starting to consume completable process...");
        return webClient.calibrateSensors()
                .doOnSubscribe(subscription -> logger.info("[Completable] Subscription started."))
                .doOnError(throwable -> logger.error("[Completable] Error occurred while processing: {}", throwable.getMessage()))
                .doOnComplete(() -> logger.info("[Completable] Completed successfully."));
    }
}
