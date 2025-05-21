package com.example.latte_api.ticket.dto;

import com.example.latte_api.ticket.enums.Priority;
import com.example.latte_api.ticket.enums.Status;

public record TicketRequest(String title, String description, Priority priority, Status status, String assignedTo, Long clientId) {
  
}
