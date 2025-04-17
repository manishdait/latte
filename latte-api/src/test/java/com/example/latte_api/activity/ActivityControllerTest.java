package com.example.latte_api.activity;

import java.time.Instant;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.example.latte_api.activity.dto.ActivityDto;
import com.example.latte_api.activity.enums.ActivityType;
import com.example.latte_api.auth.dto.AuthRequest;
import com.example.latte_api.auth.dto.AuthResponse;
import com.example.latte_api.role.Role;
import com.example.latte_api.role.RoleRepository;
import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.ticket.Ticket;
import com.example.latte_api.ticket.TicketRepository;
import com.example.latte_api.ticket.enums.Priority;
import com.example.latte_api.ticket.enums.Status;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ActivityControllerTest {
  @Container
  @ServiceConnection
  private final static PostgreSQLContainer<?> psqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine"));

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private TicketRepository ticketRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private ActivityRepository activityRepository;

  @Autowired
  private TestRestTemplate testRestTemplate;

  private Ticket ticket;

  private final String BASE_URI = "/latte-api/v1/activities";

  @BeforeEach
  void setup() {
    Role admin = roleRepository.findByRole("Admin").orElseThrow();

    User adminUser = User.builder()
      .firstname("Admin")
      .email("admin@test.in")
      .password(passwordEncoder.encode("password"))
      .role(admin)
      .build();

    userRepository.save(adminUser);

    ticket = Ticket.builder()
      .title("T1")
      .description("description")
      .createdBy(adminUser)
      .createdAt(Instant.now())
      .priority(Priority.LOW)
      .lock(false)
      .status(Status.OPEN)
      .build();
    
    ticket = ticketRepository.save(ticket);

    Activity activity = Activity.builder()
      .message("message")
      .author(adminUser)
      .ticket(ticket)
      .type(ActivityType.EDIT)
      .build();
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
  @Disabled
  void shouldReturnActivities_forTicket() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final Long id = ticket.getId();
    final ResponseEntity<PagedEntity<ActivityDto>> response = testRestTemplate.exchange(
      BASE_URI + "/ticket/" + id,
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<PagedEntity<ActivityDto>>(){}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody().getContent()).hasSize(1);
  }

  @Test
  @Disabled
  void shouldGiveNotFound_onGettingActivitiesForTicket_forInvalidId() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final long id = 300L;
    final ResponseEntity<PagedEntity<ActivityDto>> response = testRestTemplate.exchange(
      BASE_URI + "/ticket/" + id,
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<PagedEntity<ActivityDto>>(){}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldGiveForbidden_onGettingActivitiesForTicket_withRequestMissingAuthorizationHeader() {
    final Long id = ticket.getId();
    final ResponseEntity<PagedEntity<ActivityDto>> response = testRestTemplate.exchange(
      BASE_URI + "/ticket/" + id,
      HttpMethod.GET,
      new HttpEntity<>(null),
      new ParameterizedTypeReference<PagedEntity<ActivityDto>>(){}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  // Helpers

  private AuthResponse userCred() {
    final AuthRequest request = new AuthRequest("admin@test.in", "password");
    return authenticate(request);
  }

  private AuthResponse authenticate(AuthRequest request) {
    final ResponseEntity<AuthResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/auth/login",
      HttpMethod.POST,
      new HttpEntity<>(request),
      AuthResponse.class
    );

    return response.getBody();
  }
}
