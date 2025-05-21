package com.example.latte_api.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.latte_api.client.dto.ClientRequest;
import com.example.latte_api.client.dto.ClientResponse;
import com.example.latte_api.exception.OperationNotPermittedException;
import com.example.latte_api.shared.PagedEntity;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientService {
  private final ClientRepository clientRepository;

  @Transactional
  public ClientResponse createClient(ClientRequest request) {
    Client client = Client.builder()
      .name(request.name())
      .email(request.email())
      .phone(request.phone())
      .deletable(true)
      .build();
    
    clientRepository.save(client);

    return new ClientResponse(client.getId(), client.getName(), client.getEmail(), client.getPhone(), client.isDeletable());
  }

  public PagedEntity<ClientResponse> getClients(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Client> clients = clientRepository.findAll(pageable);

    PagedEntity<ClientResponse> response = new PagedEntity<>();
    response.setNext(clients.hasNext());
    response.setPrev(clients.hasPrevious());
    response.setContent(
      clients.getContent().stream()
        .map(c -> new ClientResponse(c.getId(), c.getName(), c.getEmail(), c.getPhone(), c.isDeletable()))
        .toList()
    );

    return response;
  }

  public ClientResponse getClient(Long id) {
    Client client = clientRepository.findById(id).orElseThrow(
      () -> new EntityNotFoundException("Client do not exists")
    );

    return new ClientResponse(client.getId(), client.getName(), client.getEmail(), client.getPhone(), client.isDeletable());
  }

  @Transactional
  public ClientResponse updateClient(Long id, ClientRequest request) {
    Client client = clientRepository.findById(id).orElseThrow(
      () -> new EntityNotFoundException("Client do not exists")
    );

    if (request.name() != null) {
      client.setName(request.name());
    }
    if (request.email() != null) {
      client.setEmail(request.email());
    }
    if (request.phone() != null) {
      client.setPhone(request.phone());
    }

    clientRepository.save(client);

    return new ClientResponse(client.getId(), client.getName(), client.getEmail(), client.getPhone(), client.isDeletable());
  }

  public void deleteClient(Long id) {
    Client client = clientRepository.findById(id).orElseThrow(
      () -> new EntityNotFoundException("Client do not exists")
    );

    if (!client.isDeletable()) {
      throw new OperationNotPermittedException();
    }

    clientRepository.delete(client);
  }
}
