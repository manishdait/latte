package com.example.latte_api.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.example.latte_api.client.dto.ClientRequest;
import com.example.latte_api.client.dto.ClientResponse;
import com.example.latte_api.shared.PagedEntity;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {
  private ClientService clientService;

  @Mock
  private ClientRepository clientRepository;

  @Captor
  private ArgumentCaptor<Client> clientCaptor;

  @BeforeEach
  void setup() {
    clientService = new ClientService(clientRepository);
  }

  @AfterEach
  void purge() {
    clientService = null;
  }

  @Test
  void shouldReturnPageEntity_ofClientResponse() {
    final List<Client> clients = List.of(Client.builder().id(101L).name("Bob").email("bob@test.in").build());
    @SuppressWarnings("unchecked")
    final Page<Client> response = Mockito.mock(Page.class);
    final Pageable pageable = PageRequest.of(0, 1, Sort.by(Direction.DESC, "createdAt"));
    
    final int page = 0;
    final int size = 1;

    when(clientRepository.findAll(pageable)).thenReturn(response);
    when(response.getContent()).thenReturn(clients);

    PagedEntity<ClientResponse> result = clientService.getClients(page, size);

    verify(clientRepository, times(1)).findAll(pageable);
    verify(response, times(1)).getContent();
    
    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result.getContent()).hasSize(1);
  }

  @Test
  void shouldReturnClientResponse_forGivenId() {
    final Client client = new Client(101L, "Bob", "bo@test.in", "+918989898989", true, List.of());

    final Long id = 101L;

    when(clientRepository.findById(id)).thenReturn(Optional.of(client));
    final ClientResponse result = clientService.getClient(id);
    verify(clientRepository, times(1)).findById(id);

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  void shouldThrowException_whenGetClient_forInvalidId() {
    final Long id = 101L;

    when(clientRepository.findById(id)).thenReturn(Optional.empty());
    Assertions.assertThatThrownBy(() -> clientService.getClient(id))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldCreate_newClient() {
    final Client client = Mockito.mock(Client.class);

    final ClientRequest request = new ClientRequest("Bob", "bob@test.in", "+918989898989");

    when(clientRepository.save(any(Client.class))).thenReturn(client);

    final ClientResponse result = clientService.createClient(request);

    verify(clientRepository,times(1)).save(clientCaptor.capture());

    Assertions.assertThat(result).isNotNull();

    Client capture = clientCaptor.getValue();
    Assertions.assertThat(capture.getName()).isEqualTo(request.name());
    Assertions.assertThat(capture.getEmail()).isEqualTo(request.email());
    Assertions.assertThat(capture.getPhone()).isEqualTo(request.phone());
  }

  @Test
  void shouldUpdateClient() {
    final Client updatedClient = Mockito.mock(Client.class);
    final Client client = new Client(101L, "Bob", "bo@test.in", "+918989898989", true, List.of());
    
    final ClientRequest request = new ClientRequest("NewBob", "newbob@test.in", "+912323232323");
    final Long id = 101L;

    when(clientRepository.findById(id)).thenReturn(Optional.of(client));
    when(clientRepository.save(any(Client.class))).thenReturn(updatedClient);

    final ClientResponse result = clientService.updateClient(id, request);

    verify(clientRepository, times(1)).findById(id);
    verify(clientRepository,times(1)).save(clientCaptor.capture());

    Assertions.assertThat(result).isNotNull();

    Client capture = clientCaptor.getValue();
    Assertions.assertThat(capture.getName()).isEqualTo(request.name());
    Assertions.assertThat(capture.getEmail()).isEqualTo(request.email());
    Assertions.assertThat(capture.getPhone()).isEqualTo(request.phone());
  }

  @Test
  void shouldThrowException_onUpdateClient_ifUsernotExists() {
    final ClientRequest request = new ClientRequest("NewBob", "newbob@test.in", "+912323232323");
    final Long id = 101L;

    when(clientRepository.findById(id)).thenReturn(Optional.empty());

    Assertions.assertThatThrownBy(() -> clientService.updateClient(id, request))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldDeleteUser_forGivenId() {
    final Client client = Mockito.mock(Client.class);
    final Long id = 101L;

    when(clientRepository.findById(id)).thenReturn(Optional.of(client));
    when(client.isDeletable()).thenReturn(true);
    
    clientService.deleteClient(id);

    verify(clientRepository, times(1)).findById(id);
    verify(clientRepository, times(1)).delete(client);
  }

  @Test
  void shouldThrowException_whenDeleteUser_forInvalidId() {
    final Long id = 101L;

    when(clientRepository.findById(id)).thenReturn(Optional.empty());
    
    Assertions.assertThatThrownBy(() -> clientService.deleteClient(id))
      .isInstanceOf(EntityNotFoundException.class);
  }
}
