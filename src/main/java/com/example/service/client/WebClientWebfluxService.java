package com.example.service.client;

import com.example.model.SensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class WebClientWebfluxService {
    private static final Logger logger = LoggerFactory.getLogger(WebClientWebfluxService.class);

    private final WebClient webClient;

    public WebClientWebfluxService(
            @Value("${provider.service.webflux.url}") String providerUrl,
            WebClient.Builder webClientBuilder
    ) {
        this.webClient = webClientBuilder.baseUrl(providerUrl).build();
    }

    // Flux: 0 to N
    public Flux<SensorData> fetchStreamSensorData() {
        return webClient.get()
                .uri("/observable")
                .retrieve()
                .bodyToFlux(SensorData.class);
    }

    // Flux: 0 to N
    public Flux<SensorData> fetchFastStreamSensorData() {
        return webClient.get()
                .uri("/flowable")
                .retrieve()
                .bodyToFlux(SensorData.class);
    }

    // Mono: 0 or 1
    public Mono<SensorData> fetchSingleSensorData() {
        return webClient.get()
                .uri("/single")
                .httpRequest(request -> logger.info("Calling URL: {}", request.getURI()))
                .retrieve()
                .bodyToMono(SensorData.class);
    }

    public Mono<SensorData> fetchMaybeSensorData(boolean flag) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/maybe")
                        .queryParam("flag", flag)
                        .build())
                .retrieve()
                .bodyToMono(SensorData.class);
    }

    public Mono<Void> calibrateSensors() {
        return webClient.post()
                .uri("/calibrate")
                .retrieve()
                .bodyToMono(Void.class);
    }
}
