package com.example.service;

import com.example.service.client.EventStreamClientRxApi;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventStreamClientRxService {

    private static final Logger logger = LoggerFactory.getLogger(EventStreamClientRxService.class);

    private final EventStreamClientRxApi clienRxApi;

    @Autowired
    public EventStreamClientRxService(EventStreamClientRxApi eventStreamClientRxApi) {
        this.clienRxApi = eventStreamClientRxApi;
    }

    public Flowable<String> consumeFlowable() {
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

    public Flowable<String> consumeBackpressure(
            int delay, BackpressureStrategy backpressureStrategy, boolean isFastProducer, int dataSize,
            List<String> receivedEvents) {
        logger.info("[Backpressure {}] Starting to consume flowable sensor data...", backpressureStrategy);
        return clienRxApi.getBackpressureFlowable(backpressureStrategy, isFastProducer, dataSize)
                .publish()
                .refCount() // This manage the subscription
                .doOnSubscribe(
                        subscription -> logger.info("[Backpressure {}] Subscription started.", backpressureStrategy))
                .doOnNext(
                        data -> {
                            logger.info("[Backpressure {}] Received sensor data: {}", backpressureStrategy, data);
                            receivedEvents.add(data);
                            addDelay(delay);
                        })
                .doOnError(
                        throwable -> logger.error("[Backpressure {}] Error occurred while fetching sensor data: {}",
                                backpressureStrategy, throwable.getMessage()))
                .doOnCancel(() -> logger.info("[Backpressure {}] Subscription cancelled", backpressureStrategy))
                .doOnComplete(() -> logger.info("[Backpressure {}] Completed successfully.", backpressureStrategy))
                .subscribeOn(Schedulers.io()) // Run on I/O scheduler for background processing
                .observeOn(Schedulers.single());// Observe on a single thread for printing
    }

    public Flowable<String> consumeErrorHandling(String errorHandlingFlag, List<String> receivedEvents) {
        logger.info("[ErrorHandling {}] Starting to consume flowable sensor data...", errorHandlingFlag);
        return clienRxApi.getErrorHandlingFlowable(errorHandlingFlag)
                .publish()
                .refCount() // This manage the subscription
                .doOnSubscribe(
                        subscription -> logger.info("[ErrorHandling {}] Subscription started.", errorHandlingFlag))
                .doOnNext(
                        data -> {
                            logger.info("[ErrorHandling {}] Received sensor data: {}", errorHandlingFlag, data);
                            receivedEvents.add(data);
                        })
                .doOnError(
                        throwable -> logger.error("[ErrorHandling {}] Error occurred while fetching sensor data: {}",
                                errorHandlingFlag, throwable.getMessage()))
                .doOnCancel(() -> logger.info("[ErrorHandling {}] Subscription cancelled", errorHandlingFlag))
                .doOnComplete(() -> logger.info("[ErrorHandling {}] Completed successfully.", errorHandlingFlag))
                .subscribeOn(Schedulers.io()) // Run on I/O scheduler for background processing
                .observeOn(Schedulers.single());// Observe on a single thread for printing
    }

    private static void addDelay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
