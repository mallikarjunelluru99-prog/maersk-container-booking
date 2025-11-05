package com.maersk.container.booking.client;


import com.maersk.container.booking.model.AvailabilityRequest;
import com.maersk.container.booking.model.ExternalAvailabilityResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ContainerAvailabilityClient {

    private final WebClient webClient;
    private final String path;

    public ContainerAvailabilityClient(
            WebClient.Builder builder,
            @Value("${availability.external.base-url}") String baseUrl,
            @Value("${availability.external.path}") String path) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.path = path;
    }

    public Mono<ExternalAvailabilityResponse> invokeAvailabilityApi(AvailabilityRequest request) {
        return webClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ExternalAvailabilityResponse.class);
    }
}
