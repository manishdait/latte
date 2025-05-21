package com.example.latte_api.client.dto;

public record ClientResponse(Long id, String name, String email, String phone, boolean deletable) {
  
}
