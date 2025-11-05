package com.maersk.container.booking.client;


import com.maersk.container.booking.model.AvailabilityRequest;
import com.maersk.container.booking.model.AvailabilityResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ContainerAvailabilityClient {

    public Mono<AvailabilityResponse> checkAvailability(AvailabilityRequest request) {
        boolean available = request.getQuantity() <= 100;
        return Mono.just(new AvailabilityResponse(available));
    }
}
