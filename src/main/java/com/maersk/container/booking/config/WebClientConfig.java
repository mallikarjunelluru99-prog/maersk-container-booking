package com.maersk.container.booking.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
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
                .httpResponseDecoder(spec -> spec
                        .maxHeaderSize(maxHeaderSize));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(http));
    }
}
