package com.example.latte_api.role.dto;

import java.util.List;

public record RoleRequest(String role, List<String> authorities) {
  
}
