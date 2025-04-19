package com.example.latte_api.role.dto;

import java.util.List;

public record RoleResponse(Long id, String role, List<String> authorities, boolean editable, boolean deletable) {
  
}
