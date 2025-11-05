// src/main/java/com/maersk/containerbooking/config/CircuitBreakerProperties.java
package com.maersk.container.booking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "resilience4j.custom-circuitbreaker.availability-provider")
public class CircuitBreakerProperties {

    public enum WindowType { COUNT_BASED, TIME_BASED }

    private WindowType slidingWindowType = WindowType.COUNT_BASED;
    private int slidingWindowSize = 10;                    // or time-based window size in seconds (see below)
    private int minimumNumberOfCalls = 10;

    private float failureRateThreshold = 50f;              // percent
    private float slowCallRateThreshold = 100f;            // percent (0..100)
    private Duration slowCallDurationThreshold = Duration.ofSeconds(2);

    private Duration waitDurationInOpenState = Duration.ofSeconds(10);
    private int permittedNumberOfCallsInHalfOpenState = 3;
    private boolean automaticTransitionFromOpenToHalfOpenEnabled = true;

    private boolean writableStackTraceEnabled = true;
    private int eventConsumerBufferSize = 100;

    // Optional: include/ignore specific exception classes (by FQCN)
    private List<String> recordExceptions;
    private List<String> ignoreExceptions;
}
