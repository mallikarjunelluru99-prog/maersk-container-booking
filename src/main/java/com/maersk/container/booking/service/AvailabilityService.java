package com.maersk.container.booking.service;

import com.maersk.container.booking.client.ContainerAvailabilityClient;
import com.maersk.container.booking.model.AvailabilityRequest;
import com.maersk.container.booking.model.AvailabilityResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AvailabilityService {

    private final ContainerAvailabilityClient availabilityClient;

    public AvailabilityService(ContainerAvailabilityClient availabilityClient) {
        this.availabilityClient = availabilityClient;
    }

    public Mono<AvailabilityResponse> checkAvailability(AvailabilityRequest request) {
        return availabilityClient.checkAvailability(request);
    }
}
