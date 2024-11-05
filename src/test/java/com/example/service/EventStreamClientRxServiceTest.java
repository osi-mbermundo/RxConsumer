package com.example.service;

import com.example.RxConsumerApplication;
import com.example.service.client.EventStreamClientRxApi;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.service.client.EventStreamClientRxApi.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = RxConsumerApplication.class)
@TestPropertySource(locations = "classpath:application-test.yml")
public class EventStreamClientRxServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(EventStreamClientRxServiceTest.class);
    @Autowired
    private EventStreamClientRxApi eventStreamClient;

    private EventStreamClientRxService consumerService;

    @BeforeEach
    void setUp() {
        consumerService = new EventStreamClientRxService(eventStreamClient);
    }

    @Test
    @DisplayName("Verify Flowable Consumption with OkHttpClient")
    void consumeFlowable() {
        Flowable<String> sharedFlowable = consumerService.consumeFlowable();

        assertNotNull(sharedFlowable);

        // This an RxJava TestObserver, to validate the state of the flowable
        TestSubscriber<String> testSubscriber = new TestSubscriber<>();
        sharedFlowable.subscribe(testSubscriber);


        // Use Awaitility to wait for the expected condition
        await().atMost(30, TimeUnit.SECONDS).until(() -> {
            return !testSubscriber.values().isEmpty(); // Check if at least one item is emitted
        });

        // within 30seconds I assume that it will be completed
        // we are taking only 10 records, which is emitted every seconds
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(testSubscriber::assertComplete);

        testSubscriber.assertNoErrors();
    }

    @Test
    @DisplayName("Backpressure: DROP - Fast Producer vs. Slow Consumer")
    void consumeBackpressure_DROP_scenario_1() {
        var producerTakeCount = 10; // .take() few emmitted records
        var consumerDelay = 1000; // in millis
        invokeBackPressureEndpoint(BackpressureStrategy.DROP, consumerDelay, true, producerTakeCount);
    }

    @Test
    @DisplayName("Backpressure: DROP - Slow Producer vs. Fast Consumer")
    void consumeBackpressure_DROP_scenario_2() {
        var producerTakeCount = 5; // .take() few emmitted records
        var consumerDelay = 0; // in millis (no delay)
        invokeBackPressureEndpoint(BackpressureStrategy.DROP, consumerDelay, false, producerTakeCount);
    }

    @Test
    @DisplayName("Backpressure: LATEST - Fast Producer vs. Slow Consumer")
        //  If the latest item is dropped because the consumer is too slow, you may end up receiving no data at all.
    void consumeBackpressure_LATEST_scenario_3() {
        var producerTakeCount = 10; // .take() few emmitted records
        var consumerDelay = 1000; // in millis
        invokeBackPressureEndpoint(BackpressureStrategy.LATEST, consumerDelay, true, producerTakeCount);
    }

    @Test
    @DisplayName("Backpressure: LATEST - Slow Producer vs. Fast Consumer")
    void consumeBackpressure_LATEST_scenario_4() {
        var producerTakeCount = 5; // .take() few emmitted records
        var consumerDelay = 0; // in millis (no delay)
        invokeBackPressureEndpoint(BackpressureStrategy.LATEST, consumerDelay, false, producerTakeCount);
    }

    @Test
    @DisplayName("Backpressure: BUFFER - Fast Producer vs. Slow Consumer")
        // If the latest item is dropped because the consumer is too slow, you may end up receiving no data at all.
        // I added DROP_OLDEST (Drop the oldest when buffered is full)
    void consumeBackpressure_BUFFER_scenario_5() {
        var producerTakeCount = 20; // .take() few emmitted records
        var consumerDelay = 1000; // in millis
        invokeBackPressureEndpoint(BackpressureStrategy.BUFFER, consumerDelay, true, producerTakeCount);
    }

    @Test
    @DisplayName("Backpressure: BUFFER - Slow Producer vs. Fast Consumer")
    void consumeBackpressure_BUFFER_scenario_6() {
        var producerTakeCount = 7; // .take() few emmitted records
        var consumerDelay = 0; // in millis (no delay)
        invokeBackPressureEndpoint(BackpressureStrategy.BUFFER, consumerDelay, false, producerTakeCount);
    }

    @Test
    @DisplayName("Backpressure: MISSING - Fast Producer vs. Slow Consumer")
        // ERROR: MissingBackpressureException
        // This indicates that the producer emitted an item without the consumer having requested it, causing an overflow situation
    void consumeBackpressure_MISSING_scenario_7() {
        var producerTakeCount = 5; // .take() few emmitted records
        var consumerDelay = 1000; // in millis
        invokeBackPressureEndpoint(BackpressureStrategy.MISSING, consumerDelay, true, producerTakeCount);
    }

    @Test
    @DisplayName("Backpressure: MISSING - Slow Producer vs. Fast Consumer")
    void consumeBackpressure_MISSING_scenario_8() {
        var producerTakeCount = 7; // .take() few emmitted records
        var consumerDelay = 0; // in millis (no delay)
        invokeBackPressureEndpoint(BackpressureStrategy.MISSING, consumerDelay, false, producerTakeCount);
    }


    @Test
    @DisplayName("Error Handling: onErrorReturn")
    void consume_with_errorHandling_onErrorReturn_scenario_9() {
        invokeErrorHandlingEndpoint(ERRHANDLING_ONERRORRETURN);
    }

    @Test
    @DisplayName("Error Handling: doOnError")
    void consume_with_errorHandling_doOnError_scenario_10() {
        invokeErrorHandlingEndpoint(ERRHANDLING_DOONERROR);
    }

    @Test
    @DisplayName("Error Handling: onErrorResumeNext")
    void consume_with_errorHandling_onErrorResumeNext_scenario_11() {
        invokeErrorHandlingEndpoint(ERRHANDLING_ONERRORRESUMENEXT);
    }

    /**
     * @param backpressureStrategy Flowable BackpressureStrategy
     * @param consumerDelay        controls how fast or slow the consumer processes the events
     * @param isFastProducer       Emits every 1 ms if isFastProducer is true, otherwise every 5 seconds
     * @param takeEmittedEvents    Limit the emitted records from producer
     */
    private void invokeBackPressureEndpoint(
            BackpressureStrategy backpressureStrategy, int consumerDelay, boolean isFastProducer, int takeEmittedEvents) {
        List<String> receivedEvents = new ArrayList<>();
        Flowable<String> sharedFlowable = consumerService.consumeBackpressure(
                consumerDelay, backpressureStrategy, isFastProducer, takeEmittedEvents, receivedEvents);

        assertNotNull(sharedFlowable);

        // This an RxJava TestObserver, to validate the state of the flowable
        TestSubscriber<String> testSubscriber = new TestSubscriber<>();
        sharedFlowable.subscribe(testSubscriber);

        // Simulate a slow consumer by processing each event with a delay
        testSubscriber.assertNoErrors();

        var awaitTime = isFastProducer ? 20 : 25;
        await()
                .atMost(awaitTime, TimeUnit.SECONDS)                         // Set an appropriate timeout
                .pollInterval(100, TimeUnit.MILLISECONDS)           // Poll at regular intervals
                .until(() -> receivedEvents.size() >= takeEmittedEvents);     // Wait until at least some events are received
        logger.info("Total received: " + receivedEvents.size());
    }

    private void invokeErrorHandlingEndpoint(String errorHandlingFlag) {
        List<String> receivedEvents = new ArrayList<>();
        Flowable<String> sharedFlowable = consumerService.consumeErrorHandling(errorHandlingFlag, receivedEvents);

        assertNotNull(sharedFlowable);

        // This an RxJava TestObserver, to validate the state of the flowable
        TestSubscriber<String> testSubscriber = new TestSubscriber<>();
        sharedFlowable.subscribe(testSubscriber);

        // Simulate a slow consumer by processing each event with a delay
        testSubscriber.assertNoErrors();

        await()
                .atMost(20, TimeUnit.SECONDS)                         // Set an appropriate timeout
                .pollInterval(100, TimeUnit.MILLISECONDS)           // Poll at regular intervals
                .until(() -> receivedEvents.size() >= 10);
        logger.info("Total received: " + receivedEvents.size());
    }
}
