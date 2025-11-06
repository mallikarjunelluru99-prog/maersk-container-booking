package com.maersk.container.booking.controller;

import com.maersk.container.booking.model.AvailabilityRequest;
import com.maersk.container.booking.model.AvailabilityResponse;
import com.maersk.container.booking.model.BookingRequest;
import com.maersk.container.booking.model.BookingResponse;
import com.maersk.container.booking.service.AvailabilityService;
import com.maersk.container.booking.service.BookingService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingControllerV1 {

    private final AvailabilityService availabilityService;
    private final BookingService bookingService;

    public BookingControllerV1(AvailabilityService availabilityService, BookingService bookingService) {
        this.availabilityService = availabilityService;
        this.bookingService = bookingService;
    }

    @PostMapping("/availability")
    public Mono<AvailabilityResponse> checkAvailability(@Valid @RequestBody AvailabilityRequest request) {
        return availabilityService.checkAvailability(request);
    }

    @PostMapping
    public Mono<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        return bookingService.createBooking(request);
    }
}
