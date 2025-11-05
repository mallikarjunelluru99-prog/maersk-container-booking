package com.maersk.container.booking.service;


import com.maersk.container.booking.client.ContainerAvailabilityClient;
import com.maersk.container.booking.model.AvailabilityRequest;
import com.maersk.container.booking.model.AvailabilityResponse;
import com.maersk.container.booking.model.ExternalAvailabilityResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AvailabilityServiceTest {

    private ContainerAvailabilityClient availabilityClient;
    private AvailabilityService availabilityService;

    @BeforeEach
    void setUp() {
        availabilityClient = mock(ContainerAvailabilityClient.class);

        CircuitBreaker cb = CircuitBreaker.of("availabilityProvider",
                CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .failureRateThreshold(50f)
                        .waitDurationInOpenState(Duration.ofSeconds(1))
                        .build());

        Retry retry = Retry.of("availabilityProvider",
                RetryConfig.custom()
                        .maxAttempts(3)
                        .waitDuration(Duration.ofMillis(10))
                        .retryExceptions(RuntimeException.class)
                        .build());

        availabilityService = new AvailabilityService(availabilityClient, cb, retry, true, false);
    }

    @Test
    void mapsPositiveSpaceToTrue() {
        when(availabilityClient.invokeAvailabilityApi(any()))
                .thenReturn(Mono.just(ext(5)));

        StepVerifier.create(availabilityService.checkAvailability(req()))
                .expectNextMatches(AvailabilityResponse::available)
                .verifyComplete();
    }

    @Test
    void mapsZeroSpaceToFalse() {
        when(availabilityClient.invokeAvailabilityApi(any()))
                .thenReturn(Mono.just(ext(0)));

        StepVerifier.create(availabilityService.checkAvailability(req()))
                .expectNextMatches(res -> !res.available())
                .verifyComplete();
    }


    @Test
    void fallbackWhenAlwaysFailing_flagOn_returnsFalse() {
        when(availabilityClient.invokeAvailabilityApi(any()))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        StepVerifier.create(availabilityService.checkAvailability(req()))
                .expectNextMatches(res -> !res.available()) // fallbackAvailable=false
                .verifyComplete();
    }

    private AvailabilityRequest req() {
        var r = new AvailabilityRequest();
        r.setContainerSize(20);
        r.setOrigin("Chennai");
        r.setDestination("Singapore");
        r.setQuantity(5);
        return r;
    }

    private ExternalAvailabilityResponse ext(int space) {
        return new ExternalAvailabilityResponse(space);
    }
}
