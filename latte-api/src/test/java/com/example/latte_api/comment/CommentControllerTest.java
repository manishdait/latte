package com.example.latte_api.comment;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
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

import com.example.latte_api.activity.ActivityRepository;
import com.example.latte_api.activity.dto.ActivityDto;
import com.example.latte_api.activity.enums.ActivityType;
import com.example.latte_api.auth.dto.AuthRequest;
import com.example.latte_api.auth.dto.AuthResponse;
import com.example.latte_api.comment.dto.CommentRequest;
import com.example.latte_api.handler.ErrorResponse;
import com.example.latte_api.ticket.TicketRepository;
import com.example.latte_api.ticket.dto.TicketRequest;
import com.example.latte_api.ticket.dto.TicketResponse;
import com.example.latte_api.ticket.enums.Priority;
import com.example.latte_api.ticket.enums.Status;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;
import com.example.latte_api.user.role.Role;
import com.example.latte_api.user.role.RoleRepository;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CommentControllerTest {
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

  @BeforeEach
  void setup() {
    Role user = roleRepository.findByRole("ROLE_USER").orElseThrow();
    Role admin = roleRepository.findByRole("ROLE_ADMIN").orElseThrow();

    User jhon = User.builder()
      .firstname("Admin")
      .email("admin@test.in")
      .password(passwordEncoder.encode("Admin@01"))
      .role(admin)
      .build();

    User peter = User.builder()
      .firstname("Peter")
      .email("peter@test.in")
      .password(passwordEncoder.encode("Peter@01"))
      .role(user)
      .build();

    userRepository.saveAll(List.of(jhon, peter));
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
  void shouldCreateNewComment_forTicket() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketResponse ticket = getTicket();
    final CommentRequest request = new CommentRequest("Test", ticket.id());

    final ResponseEntity<ActivityDto> response = testRestTemplate.exchange(
      "/latte-api/v1/comments",
      HttpMethod.POST,
      new HttpEntity<>(request, headers),
      ActivityDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    
    final ActivityDto result = response.getBody();
    Assertions.assertThat(result.author()).isEqualTo("Peter");
    Assertions.assertThat(result.message()).isEqualTo(request.message());
    Assertions.assertThat(result.type()).isEqualTo(ActivityType.COMMENT);
  }

  @Test
  void shouldGiveInternalServerError_onCreateNewComment_ifTicketNotExists() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final CommentRequest request = new CommentRequest("Test", 300L);

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/comments",
      HttpMethod.POST,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void shouldGiveForbidden_onCreateNewComment_requestMissingAuthorizationHeader() {
    final TicketResponse ticket = getTicket();
    final CommentRequest request = new CommentRequest("Test", ticket.id());

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/comments",
      HttpMethod.POST,
      new HttpEntity<>(request, null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private TicketResponse getTicket() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest request = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "Admin");

    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets",
      HttpMethod.POST,
      new HttpEntity<>(request, headers),
      TicketResponse.class
    );
    return response.getBody();
  }
  
  private AuthResponse userCred() {
    final AuthRequest request = new AuthRequest("peter@test.in", "Peter@01");
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
