package com.example.latte_api.ticket;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.example.latte_api.ticket.enums.Priority;
import com.example.latte_api.ticket.enums.Status;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;
import com.example.latte_api.user.role.Role;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class TicketRepositoryTest {
  @Container
  @ServiceConnection
  private final static PostgreSQLContainer<?> psqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine")); 

  @Autowired
  private TicketRepository ticketRepository;
  
  @Autowired
  private UserRepository userRepository;

  private Ticket ticket = Ticket.builder()
    .title("Test Ticket")
    .description("Ticket for test")
    .priority(Priority.LOW)
    .status(Status.OPEN)
    .build();

  @BeforeEach
  void setup() {
    User user = User.builder()
    .firstname("Peter")
    .email("peter@test.in")
    .password("Peter01")
    .role(Role.builder().id(101L).role("ROLE_USER").build())
    .build();
    userRepository.save(user);

    ticket.setCreatedBy(user);
    ticketRepository.save(ticket);
  }

  @AfterEach
  void purge() {
    ticketRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void canEstablishConnection() {
    Assertions.assertThat(psqlContainer.isCreated()).isTrue();
    Assertions.assertThat(psqlContainer.isRunning()).isTrue();
  }

  @Test
  void shouldReturn_ticketPage_ForValidUser() {
    final User user = ticket.getCreatedBy();
    final Pageable pageable = PageRequest.of(0, 1);
    final Page<Ticket> result = ticketRepository.findByCreatedBy(pageable, user);

    Assertions.assertThat(result.getContent()).hasSize(1);
  }

  @Test
  void shouldReturn_emptyTicketPage_ForInvalidUser() {
    final User user = User.builder()
      .id(1012L)
      .firstname("Louis")
      .email("louis@test.in")
      .password("LOuis01")
      .role(Role.builder().id(101L).role("ROLE_USER").build())
      .build();

    final Pageable pageable = PageRequest.of(0, 1);
    final Page<Ticket> result = ticketRepository.findByCreatedBy(pageable, user);

    System.out.println(result.getContent());
    Assertions.assertThat(result.getContent()).hasSize(0);
  }
}
