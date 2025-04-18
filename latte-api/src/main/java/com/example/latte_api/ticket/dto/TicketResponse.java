package com.example.latte_api.ticket.dto;

import java.time.Instant;

import com.example.latte_api.ticket.enums.Priority;
import com.example.latte_api.ticket.enums.Status;
import com.example.latte_api.user.dto.UserDto;

public record TicketResponse(
  Long id, 
  String title, 
  String description, 
  Priority priority, 
  Status status, 
  Boolean lock,
  UserDto createdBy, 
  UserDto assignedTo,
  Instant createdAt,
  Instant lastUpdated
) {
  
}
