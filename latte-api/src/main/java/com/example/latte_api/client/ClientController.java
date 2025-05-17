package com.example.latte_api.client;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.latte_api.client.dto.ClientRequest;
import com.example.latte_api.client.dto.ClientResponse;
import com.example.latte_api.shared.PagedEntity;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/latte-api/v1/clients")
@RequiredArgsConstructor
public class ClientController {
  private final ClientService clientService;

  @GetMapping()
  public ResponseEntity<PagedEntity<ClientResponse>> getClients(@RequestParam int page, @RequestParam int size) {
    return ResponseEntity.status(HttpStatus.OK).body(clientService.getClients(page, size));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ClientResponse> getCLinet(@PathVariable Long id) {
    return ResponseEntity.status(HttpStatus.OK).body(clientService.getClient(id));
  }

  @PostMapping()
  public ResponseEntity<ClientResponse> addClient(@RequestBody ClientRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(clientService.createClient(request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ClientResponse> updateClient(@PathVariable Long id, @RequestBody ClientRequest request) {
    return ResponseEntity.status(HttpStatus.OK).body(clientService.updateClient(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, Boolean>> deleteClinet(@PathVariable Long id) {
    clientService.deleteClient(id);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("deleted", true));
  }
}
