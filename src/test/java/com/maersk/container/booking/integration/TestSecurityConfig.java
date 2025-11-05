package com.maersk.container.booking.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@TestConfiguration
public class TestSecurityConfig {

  @Bean
  @Primary // 👈 ensures this replaces the real JwtDecoder
  public ReactiveJwtDecoder reactiveJwtDecoder() {
    return token -> {
      List<String> roles = switch (token) {
        case "cust"  -> List.of("CUSTOMER");
        case "guest" -> List.of("GUEST");
        default      -> List.of();
      };

      Jwt jwt = Jwt.withTokenValue(token)
              .header("alg", "HS256")
              .subject("test-user")
              .claim("roles", roles)
              .issuedAt(Instant.now())
              .expiresAt(Instant.now().plusSeconds(3600))
              .build();

      return Mono.just(jwt);
    };
  }
}
