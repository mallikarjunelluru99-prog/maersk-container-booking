package com.maersk.container.booking.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class WebClientConfig {

    @Bean
    WebClient.Builder webClientBuilder(
            @Value("${availability.external.connect-timeout-ms:2000}") int connectTimeoutMs,
            @Value("${availability.external.read-timeout-ms:3000}") int readTimeoutMs,
            @Value("${availability.external.max-header-size:65536}") int maxHeaderSize
    ) {
        HttpClient http = HttpClient.create()
                .followRedirect(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofMillis(readTimeoutMs))
                .doOnConnected(c ->
                        c.addHandlerLast(new ReadTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS)))
                .httpResponseDecoder(spec -> spec.maxHeaderSize(maxHeaderSize));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(http))
                .filter(logRequest())   // 👈 Add request logging
                .filter(logResponse()); // 👈 Add response logging
    }

    /**
     * Logs outgoing requests.
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            if (log.isInfoEnabled()) {
                log.info("WebClient Request: {} {}", request.method(), request.url());
                request.headers().forEach((name, values) ->
                        values.forEach(value -> log.debug("Request header: {}={}", name, value))
                );
            }
            return Mono.just(request);
        });
    }

    /**
     * Logs incoming responses.
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (log.isInfoEnabled()) {
                log.info("⬅️  WebClient Response: Status={} Headers={}", response.statusCode(), response.headers().asHttpHeaders());
            }
            return Mono.just(response);
        });
    }
}
