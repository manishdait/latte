package com.example.latte_api.activity;

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

import com.example.latte_api.activity.enums.ActivityType;
import com.example.latte_api.role.Role;
import com.example.latte_api.ticket.Ticket;
import com.example.latte_api.ticket.TicketRepository;
import com.example.latte_api.ticket.enums.Priority;
import com.example.latte_api.ticket.enums.Status;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ActivityRepositoryTest {
  @Container
  @ServiceConnection
  private final static PostgreSQLContainer<?> psqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine")); 

  @Autowired
  private ActivityRepository activityRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TicketRepository ticketRepository;

  private Activity activity = Activity.builder()
    .type(ActivityType.EDIT)
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

    Ticket ticket = Ticket.builder()
      .title("Test Ticket")
      .description("Ticket for test")
      .priority(Priority.LOW)
      .lock(false)
      .status(Status.OPEN)
      .createdBy(user)
      .build();

    ticketRepository.save(ticket);

    activity.setAuthor(user);
    activity.setTicket(ticket);
    activity.setMessage(String.format("%s created ticked", user.getFirstname()));

    activityRepository.save(activity);
  }

  @AfterEach
  void purge() {
    activityRepository.deleteAll();
    ticketRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void canEstablishConnection() {
    Assertions.assertThat(psqlContainer.isCreated()).isTrue();
    Assertions.assertThat(psqlContainer.isRunning()).isTrue();
  }

  @Test
  void shouldReturn_activityPage_forValidTicketId() {
    final Long ticketId = activity.getTicket().getId();
    final Pageable pageable = PageRequest.of(0, 1);
    final Page<Activity> result = activityRepository.findActivitesForTicket(ticketId, pageable);

    Assertions.assertThat(result.getContent()).hasSize(1);
  }

  @Test
  void shouldReturn_emptyPage_forvInalidTicketId() {
    final Long ticketId = 123L;
    final Pageable pageable = PageRequest.of(0, 1);
    final Page<Activity> result = activityRepository.findActivitesForTicket(ticketId, pageable);

    Assertions.assertThat(result.getContent()).hasSize(0);
  }
}
