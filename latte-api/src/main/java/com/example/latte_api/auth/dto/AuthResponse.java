package com.example.latte_api.auth.dto;

public record AuthResponse(String firstname, String email, String accessToken, String refreshToken, String role) {
  
}
