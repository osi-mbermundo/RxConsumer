package com.example.service;

import com.example.model.SensorData;
import com.example.service.client.WebClientWebfluxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ConsumerWebfluxService {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerWebfluxService.class);
    private final WebClientWebfluxService webClient;
    private final String baseUrl = "http://localhost:8081/producer/api/sensors";

    public ConsumerWebfluxService(WebClientWebfluxService webClient) {
        this.webClient = webClient;
    }

    public Flux<SensorData> consumeObservableAsFlux() {
        logger.info("[Observable As Flux] Starting to consume sensor data...");

        return webClient.fetchStreamSensorData()
                .doOnSubscribe(subscription -> logger.info("[Observable As Flux] Subscription started."))
                .doOnNext(data -> logger.info("[Observable As Flux] Received sensor data: {}", data))
                .doOnError(throwable -> logger.error("[Observable As Flux] Error occurred while fetching sensor data: {}", throwable.getMessage()))
                .doOnComplete(() -> logger.info("[Observable As Flux] Completed successfully."))
                .doOnCancel(() -> logger.info("[Observable As Flux] Subscription cancelled"));
    }

    public Flux<SensorData> consumeFlowableAsFlux() {
        logger.info("[Flowable As Flux] Starting to consume sensor data...");

        return webClient.fetchFastStreamSensorData()
                .doOnSubscribe(subscription -> logger.info("[Flowable As Flux] Subscription started."))
                .doOnNext(data -> logger.info("[Flowable As Flux] Received sensor data: {}", data))
                .doOnError(throwable -> logger.error("[Flowable As Flux] Error occurred while fetching sensor data: {}", throwable.getMessage()))
                .doOnComplete(() -> logger.info("[Flowable As Flux] Completed successfully."))
                .doOnCancel(() -> logger.info("[Flowable As Flux] Subscription cancelled"));
    }


    public Mono<SensorData> consumeSingleAsMono() {
        return webClient.fetchSingleSensorData()
                .doOnSubscribe(subscription -> logger.info("[Single As Mono] Subscription started."))
                .doOnNext(data -> logger.info("[Single As Mono] Received sensor data: {}", data))
                .doOnError(throwable -> logger.error("[Single As Mono] Error occurred while fetching sensor data: {}", throwable.getMessage()))
                .doOnSuccess(data -> logger.info("[Single As Mono] Completed successfully."))
                .doOnCancel(() -> logger.info("[Single As Mono] Subscription cancelled"));
    }


    public Mono<SensorData> consumeMaybeAsMono(boolean flag) {
        return webClient.fetchMaybeSensorData(flag)
                .doOnSubscribe(subscription -> logger.info("[Maybe As Mono] Subscription started."))
                .doOnNext(data -> logger.info("[Maybe As Mono] Received sensor data: {}", data))
                .doOnError(throwable -> logger.error("[Maybe As Mono] Error occurred while fetching sensor data: {}", throwable.getMessage()))
                .doOnSuccess(data -> logger.info("[Maybe As Mono] Completed successfully."))
                .doOnCancel(() -> logger.info("[Maybe As Mono] Subscription cancelled"));
    }

    public Mono<Void> consumeCompletableAsMono() {
        return webClient.calibrateSensors();
    }

    private void addThreadSleep(int millis) {
        // Optionally keep the application alive to listen for incoming data
        try {
            // Wait for some time or a condition before disposing
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread interrupted: {}", e.getMessage());
        }
    }
}
