package com.maersk.container.booking.controller;


import com.maersk.container.booking.model.AuthTokenResponse;
import com.maersk.container.booking.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/token")
    public ResponseEntity<AuthTokenResponse> generateToken(@RequestParam String username) {
        String token = jwtUtil.generateToken(username, List.of("CUSTOMER"));
        return ResponseEntity.ok(new AuthTokenResponse(token, "Bearer"));
    }
}
