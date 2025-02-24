package com.example.latte_api.auth;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.latte_api.auth.dto.AuthRequest;
import com.example.latte_api.auth.dto.AuthResponse;
import com.example.latte_api.auth.dto.RegistrationRequest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/latte-api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("/sign-up")
  public ResponseEntity<Map<String, Boolean>> registerUser(@RequestBody RegistrationRequest request) {
    authService.registerUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("user_created", true));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> authenticateUser(@RequestBody AuthRequest request) {
    return ResponseEntity.status(HttpStatus.OK).body(authService.authenticateUser(request));
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.OK).body(authService.refreshToken(request));
  }

  @PostMapping("/verify")
  public ResponseEntity<Map<String, Boolean>> verifyToken() {
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
  }
}
