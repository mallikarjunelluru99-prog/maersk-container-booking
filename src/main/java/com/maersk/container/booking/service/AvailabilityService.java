// src/main/java/com/maersk/containerbooking/service/AvailabilityService.java
package com.maersk.container.booking.service;

import com.maersk.container.booking.client.ContainerAvailabilityClient;
import com.maersk.container.booking.model.AvailabilityRequest;
import com.maersk.container.booking.model.AvailabilityResponse;
import com.maersk.container.booking.model.ExternalAvailabilityResponse;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class AvailabilityService {

    private final ContainerAvailabilityClient client;
    private final io.github.resilience4j.circuitbreaker.CircuitBreaker cb;
    private final io.github.resilience4j.retry.Retry retry;

    private final boolean fallbackEnabled;
    private final boolean fallbackAvailable;

    public AvailabilityService(
            ContainerAvailabilityClient client,
            io.github.resilience4j.circuitbreaker.CircuitBreaker availabilityCircuitBreaker,
            io.github.resilience4j.retry.Retry availabilityRetry,
            @Value("${availability.external.fallback-enabled:true}") boolean fallbackEnabled,
            @Value("${availability.external.fallback-available:false}") boolean fallbackAvailable
    ) {
        this.client = client;
        this.cb = availabilityCircuitBreaker;
        this.retry = availabilityRetry;
        this.fallbackEnabled = fallbackEnabled;
        this.fallbackAvailable = fallbackAvailable;

        this.retry.getEventPublisher().onRetry(ev ->
                log.debug("Retry attempt {}/{} for {} due to {}",
                        ev.getNumberOfRetryAttempts() + 1,
                        retry.getRetryConfig().getMaxAttempts(),
                        ev.getName(),
                        ev.getLastThrowable() != null ? ev.getLastThrowable().toString() : "unknown"));

        this.cb.getEventPublisher()
                .onStateTransition(ev -> log.warn("CB {} transition {} -> {}", ev.getCircuitBreakerName(),
                        ev.getStateTransition().getFromState(), ev.getStateTransition().getToState()))
                .onError(ev -> log.warn("CB {} recorded error: {}", ev.getCircuitBreakerName(), ev.getThrowable().toString()));
    }

    public Mono<AvailabilityResponse> checkAvailability(AvailabilityRequest req) {
        return client.invokeAvailabilityApi(req)
                .map(this::toAvailabilityResponse)
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(cb))
                .onErrorResume(ex -> {
                    if (fallbackEnabled) {
                        log.warn("Availability fallback (flag ON). cause={}", ex.toString());
                        return Mono.just(new AvailabilityResponse(fallbackAvailable));
                    } else {
                        log.error("External availability failed (flag OFF) -> available=false. cause={}", ex.toString(), ex);
                        return Mono.just(new AvailabilityResponse(false));
                    }
                });
    }

    private AvailabilityResponse toAvailabilityResponse(ExternalAvailabilityResponse ext) {
        return new AvailabilityResponse(ext.availableSpace() > 0);
    }
}
