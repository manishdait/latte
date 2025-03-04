package com.example.latte_api.ticket;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.ticket.dto.TicketPatchRequest;
import com.example.latte_api.ticket.dto.TicketRequest;
import com.example.latte_api.ticket.dto.TicketResponse;
import com.example.latte_api.ticket.enums.Status;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/latte-api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {
  private final TicketService ticketService;

  @PostMapping()
  public ResponseEntity<TicketResponse> createTicket(@RequestBody TicketRequest ticketRequest, Authentication authentication) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(ticketRequest, authentication));
  }

  @GetMapping()
  public ResponseEntity<PagedEntity<TicketResponse>> getTickets(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.OK).body(ticketService.getTickets(page, size));
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<PagedEntity<TicketResponse>> getTicketByStatus(@PathVariable Status status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.OK).body(ticketService.getTicketByStatus(status, page, size));
  }

  @GetMapping("/info")
  public ResponseEntity<Map<String, Integer>> getTicketsInfo(Authentication authentication) {
    return ResponseEntity.status(HttpStatus.OK).body(ticketService.getTicketsInfo(authentication));
  }

  @GetMapping("/{id}")
  public ResponseEntity<TicketResponse> getTicket(@PathVariable Long id) {
    return ResponseEntity.status(HttpStatus.OK).body(ticketService.getTicket(id));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<TicketResponse> editTicket(@PathVariable Long id, @RequestBody TicketPatchRequest request, Authentication authentication) {
    return ResponseEntity.status(HttpStatus.OK).body(ticketService.editTicket(id, request, authentication));
  }

  @PatchMapping("/lock/{id}")
  public ResponseEntity<TicketResponse> lockTicket(@PathVariable Long id) {
    return ResponseEntity.status(HttpStatus.OK).body(ticketService.lockTicket(id));
  }

  @PatchMapping("/unlock/{id}")
  public ResponseEntity<TicketResponse> unlockTicket(@PathVariable Long id) {
    return ResponseEntity.status(HttpStatus.OK).body(ticketService.unlockTicket(id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, Object>> deleteTicket(@PathVariable Long id) {
    ticketService.deleteTicket(id);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("key", id, "deleted", true));
  }
}
