package com.maersk.container.booking.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class ResilienceConfig {

  private final RetryProperties retryProps;
  private final CircuitBreakerProperties cbProps;

  public ResilienceConfig(CircuitBreakerProperties cbProps, RetryProperties retryProps) {
    this.cbProps = cbProps;
    this.retryProps = retryProps;
  }

  @Bean
  public CircuitBreaker availabilityCircuitBreaker() {
    List<Class<? extends Throwable>> record = toClasses(cbProps.getRecordExceptions());
    List<Class<? extends Throwable>> ignore = toClasses(cbProps.getIgnoreExceptions());

    var cfgBuilder = CircuitBreakerConfig.custom()
            .slidingWindowType(
                    cbProps.getSlidingWindowType() == CircuitBreakerProperties.WindowType.TIME_BASED
                            ? CircuitBreakerConfig.SlidingWindowType.TIME_BASED
                            : CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(cbProps.getSlidingWindowSize())
            .minimumNumberOfCalls(cbProps.getMinimumNumberOfCalls())
            .failureRateThreshold(cbProps.getFailureRateThreshold())
            .slowCallRateThreshold(cbProps.getSlowCallRateThreshold())
            .slowCallDurationThreshold(cbProps.getSlowCallDurationThreshold())
            .waitDurationInOpenState(cbProps.getWaitDurationInOpenState())
            .permittedNumberOfCallsInHalfOpenState(cbProps.getPermittedNumberOfCallsInHalfOpenState())
            .automaticTransitionFromOpenToHalfOpenEnabled(cbProps.isAutomaticTransitionFromOpenToHalfOpenEnabled())
            .writableStackTraceEnabled(cbProps.isWritableStackTraceEnabled())
            .recordExceptions(record.toArray(new Class[0]))
            .ignoreExceptions(ignore.toArray(new Class[0]));

    var cfg = cfgBuilder.build();

    log.info("Configuring CB[availabilityProvider]: {}", cbProps);
    return CircuitBreakerRegistry.of(cfg).circuitBreaker("availabilityProvider");
  }

  @SuppressWarnings("unchecked")
  @Bean
  public Retry availabilityRetry() {
    List<Class<? extends Throwable>> retryClasses = loadClasses(retryProps.getRetryExceptions());
    List<Class<? extends Throwable>> ignoreClasses = loadClasses(retryProps.getIgnoreExceptions());

    log.info("Configuring Retry[availabilityProvider]: maxAttempts={}, wait={}, retryOn={}, ignore={}",
            retryProps.getMaxAttempts(), retryProps.getWaitDuration(), retryClasses, ignoreClasses);

    var cfg = RetryConfig.<Throwable>custom()
            .maxAttempts(retryProps.getMaxAttempts())
            .waitDuration(retryProps.getWaitDuration())
            .retryExceptions(retryClasses.toArray(new Class[0]))
            .ignoreExceptions(ignoreClasses.toArray(new Class[0]))
            .build();

    return RetryRegistry.of(cfg).retry("availabilityProvider");
  }

  @SuppressWarnings("unchecked")
  private List<Class<? extends Throwable>> loadClasses(List<String> classNames) {
    if (classNames == null) return List.of();
    return classNames.stream().map(name -> {
      try {
        return (Class<? extends Throwable>) Class.forName(name);
      } catch (ClassNotFoundException e) {
        log.warn("Could not load exception class: {}", name);
        return null;
      }
    }).filter(Objects::nonNull).collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private static List<Class<? extends Throwable>> toClasses(List<String> names) {
    if (names == null) return List.of();
    return names.stream().map(n -> {
      try {
        return (Class<? extends Throwable>) Class.forName(n);
      } catch (ClassNotFoundException e) {
        // log & skip unknown class
        return null;
      }
    }).filter(c -> c != null).collect(Collectors.toList());
  }
}
