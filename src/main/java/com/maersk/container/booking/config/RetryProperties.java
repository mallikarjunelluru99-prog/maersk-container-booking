package com.maersk.container.booking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "resilience4j.custom-retry.availability-provider")
public class RetryProperties {
    private int maxAttempts = 3;
    private Duration waitDuration = Duration.ofMillis(200);
    private List<String> retryExceptions;
    private List<String> ignoreExceptions;
}
