package com.example.latte_api.user.dto;

import com.example.latte_api.role.dto.RoleResponse;

public record UserResponse(String firstname, String email, RoleResponse role, boolean editable, boolean deletable) {
  
}
