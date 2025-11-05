package com.maersk.container.booking.controller;

import com.maersk.container.booking.model.AvailabilityRequest;
import com.maersk.container.booking.model.AvailabilityResponse;
import com.maersk.container.booking.service.AvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = BookingControllerV1.class)
@Import(BookingControllerV1Test.MockConfig.class)
class BookingControllerV1Test {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AvailabilityService availabilityService;


    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityWebFilterChain testSecurity(ServerHttpSecurity http) {
            return http
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)   // important for POST
                    .authorizeExchange(ex -> ex.anyExchange().permitAll())
                    .build();
        }
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public AvailabilityService availabilityService() {
            return Mockito.mock(AvailabilityService.class);
        }

    }

    @BeforeEach
    void setup() {
        Mockito.reset(availabilityService);
    }

    @Test
    void shouldReturnTrueWhenAvailable() {
        when(availabilityService.checkAvailability(any(AvailabilityRequest.class)))
                .thenReturn(Mono.just(new AvailabilityResponse(true)));

        webTestClient.post()
                .uri("/api/v1/bookings/check-availability")
                .header("Authorization", "Bearer whatever")
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
    void shouldReturnFalseWhenUnavailable() {
        when(availabilityService.checkAvailability(any(AvailabilityRequest.class)))
                .thenReturn(Mono.just(new AvailabilityResponse(false)));

        webTestClient.post()
                .uri("/api/v1/bookings/check-availability")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {"containerType":"DRY","containerSize":20,"origin":"Chennai",
                    "destination":"Singapore","quantity":150}
                    """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.available").isEqualTo(false);
    }

    @Test
    void shouldReturnAvailableFalseWhenQuantityExceedsLimit() {
        when(availabilityService.checkAvailability(any(AvailabilityRequest.class)))
                .thenReturn(Mono.just(new AvailabilityResponse(false)));

        webTestClient.post()
                .uri("/api/v1/bookings/check-availability")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                      "containerType": "DRY",
                      "containerSize": 40,
                      "origin": "Chennai",
                      "destination": "Singapore",
                      "quantity": 150
                    }
                    """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.available").isEqualTo(false);

        verify(availabilityService).checkAvailability(any(AvailabilityRequest.class));
    }
}
