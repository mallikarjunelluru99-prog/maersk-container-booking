package com.maersk.container.booking.controller;

import com.maersk.container.booking.model.AvailabilityRequest;
import com.maersk.container.booking.model.AvailabilityResponse;
import com.maersk.container.booking.model.BookingRequest;
import com.maersk.container.booking.model.BookingResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingControllerV1 {

    @PostMapping("/check-availability")
    public Mono<AvailabilityResponse> checkAvailability(@RequestBody AvailabilityRequest request) {
        // TODO: Replace with external API integration
        return Mono.just(new AvailabilityResponse(true));
    }

    @PostMapping
    public Mono<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        // TODO: Implement booking creation
        return Mono.just(new BookingResponse("957000001", "v1"));
    }
}
