package com.example.latte_api.user.dto;

public record UserDto(String firstname, String email, String role, boolean editable, boolean deletable) {
  
}
