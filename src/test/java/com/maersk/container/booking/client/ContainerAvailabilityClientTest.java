package com.maersk.container.booking.client;

import com.maersk.container.booking.model.AvailabilityRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ContainerAvailabilityClientTest {

  static MockWebServer server;

  @BeforeAll
  static void start() throws IOException {
    server = new MockWebServer();
    server.start();
  }

  @AfterAll
  static void stop() throws IOException {
    server.shutdown();
  }

  @Test
  void postsRequest_andParsesAvailableSpace() throws Exception {
    var client = new ContainerAvailabilityClient(
        WebClient.builder(),
        "http://localhost:" + server.getPort(),
        "/api/bookings/checkAvailable"
    );

    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody("{\"availableSpace\":4}")
        .addHeader("Content-Type","application/json"));

    StepVerifier.create(client.invokeAvailabilityApi(validReq()))
        .expectNextMatches(ext -> ext.availableSpace() == 4)
        .verifyComplete();

    var req = server.takeRequest();
    assertThat(req.getMethod()).isEqualTo("POST");
    assertThat(req.getPath()).isEqualTo("/api/bookings/checkAvailable");
  }

  private AvailabilityRequest validReq() {
    var r = new AvailabilityRequest();
    r.setContainerSize(20);
    r.setOrigin("Chennai");
    r.setDestination("Singapore");
    r.setQuantity(5);
    return r;
  }
}
