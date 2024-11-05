package com.example.service;

import com.example.RxConsumerApplication;
import com.example.model.SensorData;
import com.example.service.client.WebClientWebfluxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = RxConsumerApplication.class)
@TestPropertySource(locations = "classpath:application-test.yml")
class ConsumerWebfluxServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerWebfluxServiceTest.class);
    @Autowired
    private WebClientWebfluxService webClientService;

    private ConsumerWebfluxService consumerService;

    @BeforeEach
    void setUp() {
        consumerService = new ConsumerWebfluxService(webClientService);
    }

    @Test
    @DisplayName("Consume Observable as Spring webflux: Flux")
    void consumeObservableAsFlux() {
        Flux<SensorData> observableAsFlux = consumerService.consumeObservableAsFlux();

        List<SensorData> emittedData = Collections.synchronizedList(new ArrayList<>());

        observableAsFlux.subscribe(
                data -> {
                    logger.info("[Observable As Flux] Processing received sensor data: {}", data);
                    emittedData.add(data);  // Add data to the list
                },
                throwable -> logger.error("[Observable As Flux] Error during data processing: {}", throwable.getMessage()),
                () -> logger.info("[Observable As Flux] All sensor data processed and completed."));

        await().atMost(30, TimeUnit.SECONDS).until(() -> emittedData.size() >= 10);
    }

    @Test
    @DisplayName("Consume Flowable as Spring webflux: Flux")
    void consumeFlowableAsFlux() {
        Flux<SensorData> flowableAsFlux = consumerService.consumeFlowableAsFlux();

        List<SensorData> emittedData = Collections.synchronizedList(new ArrayList<>());

        flowableAsFlux.subscribe(
                data -> {
                    logger.info("[Flowable As Flux] Processing received sensor data: {}", data);
                    emittedData.add(data);  // Add data to the list
                },
                throwable -> logger.error("[Flowable As Flux] Error during data processing: {}", throwable.getMessage()),
                () -> logger.info("[Flowable As Flux] All sensor data processed and completed."));

        await().atMost(30, TimeUnit.SECONDS).until(() -> emittedData.size() >= 10);
    }

    @Test
    @DisplayName("Consume Single as Spring webflux: Mono")
    void consumeSingleAsMono() {
        Mono<SensorData> singleAsMono = consumerService.consumeSingleAsMono();

        List<SensorData> emittedData = Collections.synchronizedList(new ArrayList<>());

        singleAsMono.subscribe(
                data -> {
                    logger.info("[Single As Mono] Processing received sensor data: {}", data);
                    emittedData.add(data);  // Add data to the list
                },
                throwable -> logger.error("[Single As Mono] Error during data processing: {}", throwable.getMessage()),
                () -> logger.info("[Single As Mono] All sensor data processed and completed."));

        await().atMost(30, TimeUnit.SECONDS).until(() -> !emittedData.isEmpty());
    }

    @Test
    @DisplayName("Consume Maybe as Spring webflux: Mono")
    void consumeMaybeAsMono_with_value() {
        Mono<SensorData> maybeAsMono = consumerService.consumeMaybeAsMono(true);

        List<SensorData> emittedData = Collections.synchronizedList(new ArrayList<>());

        maybeAsMono.subscribe(
                data -> {
                    logger.info("[Maybe As Mono] Processing received sensor data: {}", data);
                    emittedData.add(data);  // Add data to the list
                },
                throwable -> logger.error("[Maybe As Mono] Error during data processing: {}", throwable.getMessage()),
                () -> logger.info("[Maybe As Mono] All sensor data processed and completed."));

        await().atMost(30, TimeUnit.SECONDS).until(() -> !emittedData.isEmpty());
    }

    @Test
    @DisplayName("Consume Maybe as Spring webflux: Mono")
    void consumeMaybeAsMono_with_empty() {
        Mono<SensorData> maybeAsMono = consumerService.consumeMaybeAsMono(false);

        List<SensorData> emittedData = Collections.synchronizedList(new ArrayList<>());

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            maybeAsMono.subscribe(
                    data -> {
                        logger.info("[Maybe As Mono] Processing received sensor data: {}", data);
                        emittedData.add(data);  // Add data to the list
                    },
                    throwable -> logger.error("[Maybe As Mono] Error during data processing: {}", throwable.getMessage()),
                    () -> logger.info("[Maybe As Mono] All sensor data processed and completed."));

            assertDoesNotThrow(() -> maybeAsMono.block(), "Expected the Mono to complete without throwing an exception.");
        });

        assertTrue(emittedData.isEmpty());
    }


    @Test
    @DisplayName("Consume Completable as Spring webflux: Mono")
    void consumeCompletableAsMono() {
        Mono<Void> completableAsMono = consumerService.consumeCompletableAsMono();

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            completableAsMono.subscribe(
                    unused -> logger.info("[Completable As Mono] Calibration completed successfully."),
                    throwable -> logger.error("[Completable As Mono] Error during calibration: " + throwable.getMessage())
            );
            assertDoesNotThrow(() -> completableAsMono.block(), "Expected the Mono to complete without throwing an exception.");
        });
    }
}
