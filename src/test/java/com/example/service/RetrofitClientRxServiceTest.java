package com.example.service;

import com.example.RxConsumerApplication;
import com.example.model.SensorData;
import com.example.service.client.EventStreamClientRxApi;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import retrofit2.Retrofit;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = RxConsumerApplication.class)
@TestPropertySource(locations = "classpath:application-test.yml")
@Disabled("The Retrofit is designed to get all values then completed")
public class RetrofitClientRxServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(RetrofitClientRxServiceTest.class);
    @Autowired
    private Retrofit retrofit;

    @Autowired
    private EventStreamClientRxApi eventStreamClient;

    private RetrofitClientRxService consumerService;

    @BeforeEach
    void setUp() {
        consumerService = new RetrofitClientRxService(retrofit);
    }

    @Test
    @DisplayName("Consume Observable using Retrofit")
    void consumeObservable() {
        // Observable is called in the producer, but since RestTemplate is synchronous (request-response)
        // It waits for the stream to finished, and not best to use for stream events
        Observable<SensorData> sharedObservable = consumerService.consumeObservable();
        assertNotNull(sharedObservable);


        // This an RxJava TestObserver, to validate the state of the observable
        TestObserver<SensorData> testObserver = new TestObserver<>();
        sharedObservable.subscribe(testObserver);


        // Use Awaitility to wait for the expected condition
        await().atMost(30, TimeUnit.SECONDS).until(() -> {
            return !testObserver.values().isEmpty(); // Check if at least one item is emitted
        });

        // within 30seconds I assume that it will be completed
        // we are taking only 10 records, which is emitted every seconds
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(testObserver::assertComplete);

        testObserver.assertNoErrors();

    }

    @Test
    @DisplayName("Consume Flowable using Retrofit")
    void consumeFlowable() {
        Flowable<SensorData> sharedFlowable = consumerService.consumeFlowable();

        assertNotNull(sharedFlowable);

        // This an RxJava TestObserver, to validate the state of the flowable
        TestSubscriber<SensorData> testSubscriber = new TestSubscriber<>();
        sharedFlowable.subscribe(testSubscriber);


        // Use Awaitility to wait for the expected condition
        await().atMost(60, TimeUnit.SECONDS).until(() -> {
            return !testSubscriber.values().isEmpty(); // Check if at least one item is emitted
        });

        // within 30seconds I assume that it will be completed
        // we are taking only 10 records, which is emitted every seconds
        await().atMost(60, TimeUnit.SECONDS).untilAsserted(testSubscriber::assertComplete);

        testSubscriber.assertNoErrors();
    }

    @Test
    // We can use RestTemplate as well, behaves like a normal request-response
    @DisplayName("Consume Single using Retrofit")
    void consumeSingle() {
        Single<SensorData> singleData = consumerService.consumeSingle();
        assertNotNull(singleData);

        // This an RxJava TestObserver, to validate the state of the single, maybe and completable
        TestObserver<SensorData> testObserver = new TestObserver<>();
        singleData.subscribe(testObserver);

        // Use Awaitility to wait for the expected condition
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            return !testObserver.values().isEmpty(); // Check if at least one item is emitted
        });

        // within 10seconds I assume that it will be completed
        // we are taking only 1 record
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(testObserver::assertComplete);

        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
        testObserver.assertValue(sensorData ->
                sensorData.id().equals("sensor-single")
        );
    }

    @Test
    // We can use RestTemplate as well, behaves like a normal request-response
    @DisplayName("Consume Maybe using Retrofit, with flag true means with data response")
    void consumeMaybe_with_value() {
        Maybe<SensorData> maybeData = consumerService.consumeMaybe(true);
        assertNotNull(maybeData);

        // This an RxJava TestObserver, to validate the state of the single, maybe and completable
        TestObserver<SensorData> testObserver = new TestObserver<>();
        maybeData.subscribe(testObserver);

        // Use Awaitility to wait for the expected condition
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            return !testObserver.values().isEmpty(); // Check if at least one item is emitted
        });

        // within 10seconds I assume that it will be completed
        // we are taking only 1 record
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(testObserver::assertComplete);

        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
        testObserver.assertValue(sensorData ->
                sensorData.id().equals("sensor-maybe")
        );
    }

    @Test
    // We can use RestTemplate as well, behaves like a normal request-response
    @DisplayName("Consume Maybe using Retrofit, with flag false means with EMPTY data response")
    void consumeMaybe_with_empty() {
        Maybe<SensorData> maybeData = consumerService.consumeMaybe(false);
        assertNotNull(maybeData);

        // This an RxJava TestObserver, to validate the state of the single, maybe and completable
        TestObserver<SensorData> testObserver = new TestObserver<>();
        maybeData.subscribe(testObserver);

        // Use Awaitility to wait for the expected condition
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            return testObserver.values().isEmpty(); // This should be empty
        });

        // within 10seconds I assume that it will be completed
        // we are taking only 1 record
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(testObserver::assertComplete);

        testObserver.assertNoErrors();
        testObserver.assertValueCount(0);
    }

    @Test
    // We can use RestTemplate as well, behaves like a normal request-response
    @DisplayName("Consume Completable using Retrofit")
    void consumeCompletable() {
        Completable completableData = consumerService.consumeCompletable();
        assertNotNull(completableData);

        // This an RxJava TestObserver, to validate the state of the single, maybe and completable
        TestObserver<SensorData> testObserver = new TestObserver<>();
        completableData.subscribe(testObserver);

        // Use Awaitility to wait for the expected condition
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            return testObserver.values().isEmpty(); // This should be empty
        });

        // within 10seconds I assume that it will be completed
        // based on the code, processing will only takes 2 seconds // TimeUnit.SECONDS.sleep(2)
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(testObserver::assertComplete);

        testObserver.assertNoErrors();
        testObserver.assertValueCount(0);
    }


    @Test
    @DisplayName("Consume Backpressure using Retrofit - DROP")
    void consumeBackpressure() {
        Flowable<SensorData> sharedFlowable = consumerService.consumeBackpressure(
                BackpressureStrategy.DROP.name(), true);

        assertNotNull(sharedFlowable);

        // This an RxJava TestObserver, to validate the state of the flowable
        TestSubscriber<SensorData> testSubscriber = new TestSubscriber<>();
        sharedFlowable.subscribe(testSubscriber);

        // Use Awaitility to wait until 30 items have been emitted
//        await().untilAsserted(() -> {
//            // Ensure that 30 items have been emitted
////            assertFalse(testSubscriber.values().isEmpty(), "No items were emitted.");
//            assertEquals(30, testSubscriber.awaitCount(30), "Expected 30 items to be emitted.");
//        });

        // After ensuring that we have received 30 records, assert completion
//        await().untilAsserted(() -> {
//            testSubscriber.assertComplete(); // Assert that the flowable has completed
//        });

        await().atMost(60, TimeUnit.SECONDS).untilAsserted(testSubscriber::assertComplete);
        // Assert that no errors occurred during the subscription
        testSubscriber.assertNoErrors();
    }
}
