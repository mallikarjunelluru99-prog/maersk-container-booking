package com.maersk.container.booking.controller;

import com.maersk.container.booking.exception.GlobalExceptionHandler;
import com.maersk.container.booking.model.*;
import com.maersk.container.booking.service.AvailabilityService;
import com.maersk.container.booking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingControllerV1Test {

    @Mock AvailabilityService availabilityService;
    @Mock BookingService bookingService;

    @InjectMocks BookingControllerV1 controller;

    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(controller)
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
    }


    @Test
    void shouldReturnTrueWhenAvailable() {
        when(availabilityService.checkAvailability(any(AvailabilityRequest.class)))
                .thenReturn(Mono.just(new AvailabilityResponse(true)));

        webTestClient.post()
                .uri("/api/v1/bookings/availability")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                  {"containerType":"DRY","containerSize":20,"origin":"Chennai",
                   "destination":"Singapore","quantity":10}
                """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.available").isEqualTo(true);
    }

    @Test
    void availability_shouldReturn400_onValidationErrors() {
        // invalid enum, invalid size, short origin/destination, bad quantity, etc.
        webTestClient.post()
                .uri("/api/v1/bookings/availability")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                  {"containerType":"DRYX","containerSize":22,"origin":"A",
                   "destination":"","quantity":0}
                """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
                .jsonPath("$.details").isArray();
    }


    @Test
    void createBooking_returnsBookingRef_onSuccess() {
        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenReturn(Mono.just(new BookingResponse("957000123")));

        webTestClient.post()
                .uri("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                  {"containerType":"DRY","containerSize":20,"origin":"Chennai",
                   "destination":"Singapore","quantity":5,"timestamp":"2025-11-06T10:00:00Z"}
                """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.bookingRef").isEqualTo("957000123");
    }

    @Test
    void createBooking_returns400_onValidationErrors() {
        webTestClient.post()
                .uri("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                  {"containerType":"REEFERR","containerSize":25,"origin":"Ch",
                   "destination":"","quantity":500,"timestamp":"bad"}
                """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
                .jsonPath("$.details").isArray();
    }
}
