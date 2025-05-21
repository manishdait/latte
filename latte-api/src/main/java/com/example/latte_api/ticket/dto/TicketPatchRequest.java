package com.example.latte_api.ticket.dto;

import com.example.latte_api.ticket.enums.Priority;
import com.example.latte_api.ticket.enums.Status;

public record TicketPatchRequest(String title, String description, String assignedTo, Priority priority, Status status, Long clientId) {
  
}
