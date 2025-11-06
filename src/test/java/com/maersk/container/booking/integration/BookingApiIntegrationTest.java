package com.maersk.container.booking.integration;

import com.maersk.container.booking.config.MongoTestContainerConfig;
import com.maersk.container.booking.MaerskContainerBookingApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookingApiIntegrationTest extends MongoTestContainerConfig {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldCreateBookingAndReturnBookingRef() {

        webTestClient.post()
                .uri("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + generateValidToken())
                .bodyValue("""
                {
                    "containerType": "DRY",
                    "containerSize": 20,
                    "origin": "Chennai",
                    "destination": "Singapore",
                    "quantity": 5,
                    "timestamp": "2025-11-06T10:00:00Z"
                }
            """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.bookingRef").isNotEmpty();
    }

    private String generateValidToken() {
        return "eyJhbGciOiJIUzM4NCJ9...";
    }
}
