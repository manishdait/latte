package com.example.latte_api.auth.dto;

import com.example.latte_api.role.dto.RoleResponse;

public record AuthResponse(String firstname, String email, String accessToken, String refreshToken, RoleResponse role) {
  
}
