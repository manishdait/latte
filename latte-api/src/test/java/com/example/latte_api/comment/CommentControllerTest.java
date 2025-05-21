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
import com.example.latte_api.comment.dto.CommentDto;
import com.example.latte_api.handler.ErrorResponse;
import com.example.latte_api.role.Role;
import com.example.latte_api.role.RoleRepository;
import com.example.latte_api.ticket.TicketRepository;
import com.example.latte_api.ticket.dto.TicketRequest;
import com.example.latte_api.ticket.dto.TicketResponse;
import com.example.latte_api.ticket.enums.Priority;
import com.example.latte_api.ticket.enums.Status;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;

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

  private final String BASE_URI = "/latte-api/v1/comments";

  @BeforeEach
  void setup() {
    Role user = roleRepository.findByRole("User").orElseThrow();
    Role admin = roleRepository.findByRole("Admin").orElseThrow();

    User adminUser = User.builder()
      .firstname("Admin")
      .email("admin@test.in")
      .password(passwordEncoder.encode("password"))
      .role(admin)
      .build();

    User commonUser = User.builder()
      .firstname("User")
      .email("common@test.in")
      .password(passwordEncoder.encode("password"))
      .role(user)
      .build();

    userRepository.saveAll(List.of(adminUser, commonUser));
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
    final CommentDto request = new CommentDto("Test", ticket.id());

    final ResponseEntity<ActivityDto> response = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(request, headers),
      ActivityDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    
    final ActivityDto result = response.getBody();
    Assertions.assertThat(result.author()).isEqualTo("User");
    Assertions.assertThat(result.message()).isEqualTo(request.message());
    Assertions.assertThat(result.type()).isEqualTo(ActivityType.COMMENT);
  }

  @Test
  void shouldGiveNotFound_onCreateNewComment_ifTicketNotExists() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final CommentDto request = new CommentDto("Test", 300L);

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldGiveForbidden_onCreateNewComment_requestMissingAuthorizationHeader() {
    final TicketResponse ticket = getTicket();
    final CommentDto request = new CommentDto("Test", ticket.id());

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(request, null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldUpdateComment_forTicket() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketResponse ticket = getTicket();
    final CommentDto request = new CommentDto("Test", ticket.id());

    final ActivityDto activity= testRestTemplate.exchange(
            BASE_URI,
            HttpMethod.POST,
            new HttpEntity<>(request, headers),
            ActivityDto.class
    ).getBody();

    final CommentDto updateReq = new CommentDto("New", ticket.id());

    final ResponseEntity<ActivityDto> response = testRestTemplate.exchange(
            BASE_URI + "/" + activity.id(),
            HttpMethod.PATCH,
            new HttpEntity<>(updateReq, headers),
            ActivityDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    final ActivityDto result = response.getBody();
    Assertions.assertThat(result.message()).isEqualTo(updateReq.message());
  }

  @Test
  void shouldGiveBadRequest_IfUserNotOwner() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketResponse ticket = getTicket();
    final CommentDto request = new CommentDto("Test", ticket.id());

    final ActivityDto activity= testRestTemplate.exchange(
            BASE_URI,
            HttpMethod.POST,
            new HttpEntity<>(request, headers),
            ActivityDto.class
    ).getBody();

    final CommentDto updateReq = new CommentDto("New", ticket.id());
    final AuthResponse _cred = adminCred();

    final HttpHeaders _headers = new HttpHeaders();
   _headers.add("Authorization", "Bearer " + _cred.accessToken());

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
            BASE_URI + "/" + activity.id(),
            HttpMethod.PATCH,
            new HttpEntity<>(updateReq, _headers),
            ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
    void shouldGiveNotFound_forInvalidId() {
    final AuthResponse cred = userCred();
      final CommentDto updateReq = new CommentDto("New", 101L);

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final long id = 200;
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
            BASE_URI + "/" + id,
            HttpMethod.PATCH,
            new HttpEntity<>(updateReq, headers),
            ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldDeleteComment_forTicket() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketResponse ticket = getTicket();
    final CommentDto request = new CommentDto("Test", ticket.id());

    final ActivityDto activity= testRestTemplate.exchange(
            BASE_URI,
            HttpMethod.POST,
            new HttpEntity<>(request, headers),
            ActivityDto.class
    ).getBody();

    final ResponseEntity<ActivityDto> response = testRestTemplate.exchange(
            BASE_URI + "/" + activity.id(),
            HttpMethod.DELETE,
            new HttpEntity<>(null, headers),
            ActivityDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldGiveBadRequest_forTicket_ifNotOwner() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketResponse ticket = getTicket();
    final CommentDto request = new CommentDto("Test", ticket.id());

    final ActivityDto activity= testRestTemplate.exchange(
            BASE_URI,
            HttpMethod.POST,
            new HttpEntity<>(request, headers),
            ActivityDto.class
    ).getBody();

    final AuthResponse _cred = adminCred();

    final HttpHeaders _headers = new HttpHeaders();
    _headers.add("Authorization", "Bearer " + _cred.accessToken());

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
            BASE_URI + "/" + activity.id(),
            HttpMethod.DELETE,
            new HttpEntity<>(null, _headers),
            ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldGiveNotFound_forTicket_ifInvalidId() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    long id = 200L;

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
            BASE_URI + "/" + id,
            HttpMethod.DELETE,
            new HttpEntity<>(null, headers),
            ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  // Helpers

  private TicketResponse getTicket() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest request = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null, null);

    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets",
      HttpMethod.POST,
      new HttpEntity<>(request, headers),
      TicketResponse.class
    );
    return response.getBody();
  }
  
  private AuthResponse userCred() {
    final AuthRequest request = new AuthRequest("common@test.in", "password");
    return authenticate(request);
  }

  private AuthResponse adminCred() {
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
