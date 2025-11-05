package com.maersk.container.booking.service;


import com.maersk.container.booking.client.ContainerAvailabilityClient;
import com.maersk.container.booking.model.AvailabilityRequest;
import com.maersk.container.booking.model.AvailabilityResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AvailabilityServiceTest {

    private ContainerAvailabilityClient availabilityClient;
    private AvailabilityService availabilityService;

    @BeforeEach
    void setUp() {
        availabilityClient = mock(ContainerAvailabilityClient.class);
        availabilityService = new AvailabilityService(availabilityClient);
    }

    @Test
    void shouldReturnAvailableTrueWhenQuantityWithinLimit() {
        AvailabilityRequest request = new AvailabilityRequest();
        request.setQuantity(10);

        when(availabilityClient.checkAvailability(any()))
                .thenReturn(Mono.just(new AvailabilityResponse(true)));

        StepVerifier.create(availabilityService.checkAvailability(request))
                .expectNextMatches(AvailabilityResponse::isAvailable)
                .verifyComplete();

        verify(availabilityClient, times(1)).checkAvailability(any());
    }

    @Test
    void shouldReturnAvailableFalseWhenQuantityExceedsLimit() {
        AvailabilityRequest request = new AvailabilityRequest();
        request.setQuantity(150);

        when(availabilityClient.checkAvailability(any()))
                .thenReturn(Mono.just(new AvailabilityResponse(false)));

        StepVerifier.create(availabilityService.checkAvailability(request))
                .expectNextMatches(resp -> !resp.isAvailable())
                .verifyComplete();

        verify(availabilityClient, times(1)).checkAvailability(any());
    }
}
