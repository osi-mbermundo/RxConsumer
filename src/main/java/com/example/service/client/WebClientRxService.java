package com.example.service.client;

import com.example.model.SensorData;
import io.reactivex.rxjava3.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class WebClientRxService {
    private static final Logger logger = LoggerFactory.getLogger(WebClientRxService.class);

    private final WebClient webClient;

    public WebClientRxService(
            @Value("${provider.service.rx.url}") String providerUrl,
            WebClient.Builder webClientBuilder
    ) {
        this.webClient = webClientBuilder.baseUrl(providerUrl).build();
    }

    // Flux: 0 to N
    public Observable<SensorData> getSensorUpdates() {
        return Observable.create(emitter -> webClient.get()
                .uri("/observable")
                .retrieve()
                .bodyToFlux(SensorData.class)
                .subscribe(
                        emitter::onNext,       // Emit the data to the Flowable
                        emitter::onError,      // Handle errors
                        emitter::onComplete     // Complete the Flowable
                )
        );
    }

    // Flux: 0 to N
    public Flowable<SensorData> getFastSensorUpdates() {
        return Flowable.create(emitter -> {
            webClient.get()
                    .uri("/flowable")
                    .retrieve()
                    .bodyToFlux(SensorData.class)
                    .subscribe(
                            emitter::onNext,
                            emitter::onError,
                            emitter::onComplete
                    );
        },
                BackpressureStrategy.BUFFER // Collects all items until they can be processed; Possible OutOfMemory
//                BackpressureStrategy.DROP // Process the recent or current items, but potentially not the latest;
                // Drop the emitted items if during that time the consumer is not ready
//                BackpressureStrategy.LATEST // Process the latest item and discards any intermediate items that are not processed
//                BackpressureStrategy.ERROR // Throw an exception MissingBackpressureException
//                BackpressureStrategy.MISSING // No backpressure handling
        );
    }

    // Mono: 0 or 1
    public Single<SensorData> getSingleSensorUpdate() {
        return Single.create(emitter -> {
            webClient.get()
                    .uri("/single")
                    .retrieve()
                    .bodyToMono(SensorData.class) // Mono is the reactive type used in WebFlux
                    .subscribe(
                            emitter::onSuccess, // Emit the result if successful
                            emitter::onError     // Emit the error if something goes wrong
                    );
        });
    }

    public Maybe<SensorData> getMaybeSensorUpdate(boolean flag) {
        return Maybe.create(emitter -> {
            webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/maybe").queryParam("flag", flag).build())
                    .retrieve()
                    .bodyToMono(SensorData.class) // Mono is the reactive type used in WebFlux
                    .subscribe(
                            data -> {
                                if (data != null) {
                                    emitter.onSuccess(data); // Emit the data if available
                                } else {
                                    emitter.onComplete(); // Complete without emitting if data is null
                                }
                            },
                            emitter::onError,     // Emit the error if something goes wrong
                            emitter::onComplete    // Complete if there’s no data
                    );
        });
    }

    public Completable calibrateSensors() {
        return Completable.create(emitter -> {
            webClient.post()
                    .uri("/calibrate")
                    .retrieve()
                    .bodyToMono(Void.class) // No body expected
                    .subscribe();
        });
    }
}
