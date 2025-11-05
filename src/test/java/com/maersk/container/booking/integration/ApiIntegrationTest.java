package com.maersk.container.booking.integration;


import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
class ApiIntegrationTest {

    static MockWebServer mockServer;

    @BeforeAll
    static void start() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterAll
    static void stop() throws IOException {
        mockServer.shutdown();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        // route external HTTP calls to MockWebServer
        r.add("availability.external.base-url", () -> "http://localhost:" + mockServer.getPort());
        r.add("availability.external.path", () -> "/api/bookings/checkAvailable");
        // wide header limits (avoid Netty header size errors)
        r.add("availability.external.max-header-size", () -> 131072);
        r.add("availability.external.max-initial-line-length", () -> 16384);

        // resilience flags
        r.add("availability.external.fallback-enabled", () -> true);
        r.add("availability.external.fallback-available", () -> false);
    }

    @Autowired WebTestClient webTestClient;

    // ---------- Availability (permitAll) ----------

    @Test
    void checkAvailability_true_whenExternalHasSpace() throws Exception {
        mockServer.enqueue(json(200, "{\"availableSpace\":6}"));

        webTestClient.post()
                .uri("/api/v1/bookings/check-availability")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validAvailabilityJson())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.available").isEqualTo(true);

        var req = mockServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(req).isNotNull();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath()).isEqualTo("/api/bookings/checkAvailable");
    }

    @Test
    void checkAvailability_false_whenExternalZero() {
        mockServer.enqueue(json(200, "{\"availableSpace\":0}"));

        webTestClient.post()
                .uri("/api/v1/bookings/check-availability")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validAvailabilityJson())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.available").isEqualTo(false);
    }

    @Test
    void checkAvailability_retriesOn500_thenSucceeds() {
        mockServer.enqueue(json(500, "{}"));
        mockServer.enqueue(json(500, "{}"));
        mockServer.enqueue(json(200, "{\"availableSpace\":3}"));

        webTestClient.post()
                .uri("/api/v1/bookings/check-availability")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validAvailabilityJson())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.available").isEqualTo(true);

        assertThat(mockServer.getRequestCount()).isEqualTo(4);
    }

    @Test
    void checkAvailability_404_usesFallback_flagOn_returnsFalse() {
        mockServer.enqueue(json(404, "{}"));

        webTestClient.post()
                .uri("/api/v1/bookings/check-availability")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validAvailabilityJson())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.available").isEqualTo(false);
    }


    @Test
    void createBooking_unauthorized_withoutToken() {
        webTestClient.post()
                .uri("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validAvailabilityJson())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void createBooking_forbidden_withoutCustomerRole() {
        webTestClient.post()
                .uri("/api/v1/bookings")
                .header("Authorization", "Bearer guest")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validAvailabilityJson())
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void createBooking_okWithCustomerRole() {
        webTestClient.post()
                .uri("/api/v1/bookings")
                .header("Authorization", "Bearer cust")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validAvailabilityJson())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.bookingRef").exists()
                .jsonPath("$.apiVersion").isEqualTo("v1");
    }

    // --- helpers ---
    private static MockResponse json(int status, String body) {
        return new MockResponse()
                .setResponseCode(status)
                .setBody(body)
                .addHeader("Content-Type", "application/json");
    }

    private String validAvailabilityJson() {
        return """
      {"containerType":"DRY","containerSize":20,"origin":"Chennai","destination":"Singapore","quantity":5}
      """;
    }
}
