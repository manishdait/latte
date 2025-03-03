package com.example.latte_api.ticket;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import com.example.latte_api.auth.dto.AuthRequest;
import com.example.latte_api.auth.dto.AuthResponse;
import com.example.latte_api.handler.ErrorResponse;
import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.ticket.dto.TicketPatchRequest;
import com.example.latte_api.ticket.dto.TicketRequest;
import com.example.latte_api.ticket.dto.TicketResponse;
import com.example.latte_api.ticket.enums.Priority;
import com.example.latte_api.ticket.enums.Status;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;
import com.example.latte_api.user.dto.UserDto;
import com.example.latte_api.user.role.Role;
import com.example.latte_api.user.role.RoleRepository;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TicketControllerTest {
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

    Ticket open = Ticket.builder()
      .title("T1")
      .description("description")
      .createdBy(peter)
      .priority(Priority.LOW)
      .status(Status.OPEN)
      .build();

    Ticket close = Ticket.builder()
      .title("T2")
      .description("description")
      .createdBy(jhon)
      .priority(Priority.LOW)
      .status(Status.CLOSE)
      .build();
    
    ticketRepository.saveAll(List.of(open, close));
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
  void shouldCreateNewTicket_withoutAsigness_forAutenticatedUser() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest request = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "");

    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets",
      HttpMethod.POST,
      new HttpEntity<>(request, headers),
      TicketResponse.class
    );

    final TicketResponse result = response.getBody();
    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Assertions.assertThat(result.title()).isEqualTo(request.title());
    Assertions.assertThat(result.description()).isEqualTo(request.description());
    Assertions.assertThat(result.status()).isEqualTo(request.status());
    Assertions.assertThat(result.priority()).isEqualTo(request.priority());
    Assertions.assertThat(result.createdBy()).isEqualTo(new UserDto("Peter", "peter@test.in", "ROLE_USER"));
    Assertions.assertThat(result.assignedTo()).isNull();
  }

  @Test
  void shouldCreateNewTicket_withAsigness_forAutenticatedUser() {
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

    final TicketResponse result = response.getBody();
    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Assertions.assertThat(result.title()).isEqualTo(request.title());
    Assertions.assertThat(result.description()).isEqualTo(request.description());
    Assertions.assertThat(result.status()).isEqualTo(request.status());
    Assertions.assertThat(result.priority()).isEqualTo(request.priority());
    Assertions.assertThat(result.createdBy()).isEqualTo(new UserDto("Peter", "peter@test.in", "ROLE_USER"));
    Assertions.assertThat(result.assignedTo()).isEqualTo(new UserDto("Admin", "admin@test.in", "ROLE_ADMIN"));
  }

  @Test
  void shouldGiveBadRequest_onCreateTicket_withAsigness_ifAssigneNotExists() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest request = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "Jhon");

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets",
      HttpMethod.POST,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    Assertions.assertThat(response.getBody().error()).isEqualTo("Assinged user not found");
  }

  @Test
  void shouldGiveForbidden_onCreateTicket_forRequestMissingAuthorizationHeader() {
    final TicketRequest request = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "Admin");

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets",
      HttpMethod.POST,
      new HttpEntity<>(request, null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnPagedEntity_ofTicketResponse_ofAllTicketsCreated() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<PagedEntity<TicketResponse>> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets",
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<PagedEntity<TicketResponse>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody().getContent()).hasSize(2);
  }

  @Test
  void shouldGiveForbidden_whengettingTickets_withRequestMissingAuthorizationHeader() {
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets",
      HttpMethod.GET,
      new HttpEntity<>(null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnPagedEntity_ofTicketResponse_ofAllTicketsByStatus() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<PagedEntity<TicketResponse>> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets/status/OPEN",
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<PagedEntity<TicketResponse>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody().getContent()).hasSize(1);
  }

  @Test
  void shouldGiveForbidden_whengettingTicketsByStatus_withRequestMissingAuthorizationHeader() {
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets/status/OPEN",
      HttpMethod.GET,
      new HttpEntity<>(null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGetInfo_ofTicketsOnSystem() {
    final Map<String, Integer> expected = Map.of("open_tickets", 1, "completed_tickets", 1);
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<Map<String, Integer>> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets/info",
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<Map<String, Integer>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo(expected);
  }

  @Test
  void shouldGiveForbidden_forTicketInfo_ofTicketsOnSystem_whenRequestMissingAuhtorizationHeader() {
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets/info",
      HttpMethod.GET,
      new HttpEntity<>(null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnTicketResponse_byTicketId() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "");

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      "/latte-api/v1/tickets",
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );


    final Long id = ticket.getBody().id();
    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets/" + id,
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      TicketResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    final TicketResponse result = response.getBody();
    Assertions.assertThat(result.title()).isEqualTo(ticketRequest.title());
    Assertions.assertThat(result.description()).isEqualTo(ticketRequest.description());
    Assertions.assertThat(result.status()).isEqualTo(ticketRequest.status());
    Assertions.assertThat(result.priority()).isEqualTo(ticketRequest.priority());
    Assertions.assertThat(result.createdBy()).isEqualTo(new UserDto("Peter", "peter@test.in", "ROLE_USER"));
    Assertions.assertThat(result.assignedTo()).isNull();
  }

  @Test
  void shouldGiveInternalServerError_onGetTicketById_forInvalidId() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final Long id = 300L;
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets/" + id,
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void shouldGiveForbidden_onGetTicketById_forRequestMissingAuhtorizationHeader() {
    final Long id = 3101L;
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets/" + id,
      HttpMethod.GET,
      new HttpEntity<>(null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldUpdateTicketTitle_byTicketId() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "");

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      "/latte-api/v1/tickets",
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final TicketPatchRequest request = new TicketPatchRequest("New Test", null, null, null, null);
    final Long id = ticket.getBody().id();
    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      TicketResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    final TicketResponse result = response.getBody();
    Assertions.assertThat(result.title()).isEqualTo(request.title());
    Assertions.assertThat(result.description()).isEqualTo(ticketRequest.description());
    Assertions.assertThat(result.status()).isEqualTo(ticketRequest.status());
    Assertions.assertThat(result.priority()).isEqualTo(ticketRequest.priority());
    Assertions.assertThat(result.createdBy()).isEqualTo(new UserDto("Peter", "peter@test.in", "ROLE_USER"));
    Assertions.assertThat(result.assignedTo()).isNull();
  }

  @Test
  void shouldUpdateTicketDescription_byTicketId() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "");

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      "/latte-api/v1/tickets",
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final TicketPatchRequest request = new TicketPatchRequest(null, "new-descrip", null, null, null);
    final Long id = ticket.getBody().id();
    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      TicketResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    final TicketResponse result = response.getBody();
    Assertions.assertThat(result.title()).isEqualTo(ticketRequest.title());
    Assertions.assertThat(result.description()).isEqualTo(request.description());
    Assertions.assertThat(result.status()).isEqualTo(ticketRequest.status());
    Assertions.assertThat(result.priority()).isEqualTo(ticketRequest.priority());
    Assertions.assertThat(result.createdBy()).isEqualTo(new UserDto("Peter", "peter@test.in", "ROLE_USER"));
    Assertions.assertThat(result.assignedTo()).isNull();
  }

  @Test
  void shouldUpdateTicketAssignedTo_byTicketId() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "");

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      "/latte-api/v1/tickets",
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final TicketPatchRequest request = new TicketPatchRequest(null, null, "Admin", null, null);
    final Long id = ticket.getBody().id();
    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      TicketResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    final TicketResponse result = response.getBody();
    Assertions.assertThat(result.title()).isEqualTo(ticketRequest.title());
    Assertions.assertThat(result.description()).isEqualTo(ticketRequest.description());
    Assertions.assertThat(result.status()).isEqualTo(ticketRequest.status());
    Assertions.assertThat(result.priority()).isEqualTo(ticketRequest.priority());
    Assertions.assertThat(result.createdBy()).isEqualTo(new UserDto("Peter", "peter@test.in", "ROLE_USER"));
    Assertions.assertThat(result.assignedTo()).isEqualTo(new UserDto("Admin", "admin@test.in", "ROLE_ADMIN"));
  }

  @Test
  void shouldUpdateTicketPriority_byTicketId() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "");

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      "/latte-api/v1/tickets",
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final TicketPatchRequest request = new TicketPatchRequest(null, null, null, Priority.MEDIUM, null);
    final Long id = ticket.getBody().id();
    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      TicketResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    final TicketResponse result = response.getBody();
    Assertions.assertThat(result.title()).isEqualTo(ticketRequest.title());
    Assertions.assertThat(result.description()).isEqualTo(ticketRequest.description());
    Assertions.assertThat(result.status()).isEqualTo(ticketRequest.status());
    Assertions.assertThat(result.priority()).isEqualTo(request.priority());
    Assertions.assertThat(result.createdBy()).isEqualTo(new UserDto("Peter", "peter@test.in", "ROLE_USER"));
    Assertions.assertThat(result.assignedTo()).isNull();
  }

  @Test
  void shouldUpdateTicketStatus_byTicketId() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "");

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      "/latte-api/v1/tickets",
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final TicketPatchRequest request = new TicketPatchRequest(null, null, null, null, Status.CLOSE);
    final Long id = ticket.getBody().id();
    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      TicketResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    final TicketResponse result = response.getBody();
    Assertions.assertThat(result.title()).isEqualTo(ticketRequest.title());
    Assertions.assertThat(result.description()).isEqualTo(ticketRequest.description());
    Assertions.assertThat(result.status()).isEqualTo(request.status());
    Assertions.assertThat(result.priority()).isEqualTo(ticketRequest.priority());
    Assertions.assertThat(result.createdBy()).isEqualTo(new UserDto("Peter", "peter@test.in", "ROLE_USER"));
    Assertions.assertThat(result.assignedTo()).isNull();
  }

  @Test
  void shouldGiveInternalServerError_onUpdateTicket_byInvalidTicketId() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketPatchRequest request = new TicketPatchRequest("New Test", null, null, null, null);
    final Long id = 300L;
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void shouldGiveForbidden_onUpdateTicket_whenREquestMissingAuthorizationHeader() {
    final TicketPatchRequest request = new TicketPatchRequest("New Test", null, null, null, null);
    final Long id = 300L;
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(request, null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldDeleteTicket_byTicketId_ifRequestedByAdmin() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "Admin");

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      "/latte-api/v1/tickets",
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final Long id = ticket.getBody().id();
    final Map<String, Object> expected = Map.of("key", Integer.valueOf(String.valueOf(id)), "deleted", true);

    final ResponseEntity<Map<String, Object>> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets/" + id,
      HttpMethod.DELETE,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<Map<String, Object>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo(expected);
  }

  @Test
  void shouldGiveInternalServerError_onDeleteTicketById_forInvalidId() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final Long id = 300L;

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets/" + id,
      HttpMethod.DELETE,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void shouldGiveForbidden_onDeleteTicketById_ifRequestedNonAdmin() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "Admin");

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      "/latte-api/v1/tickets",
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final Long id = ticket.getBody().id();

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets/" + id,
      HttpMethod.DELETE,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGiveForbidden_onDeleteTicketById_ifRequestMissingAuthorizationHeader() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "Admin");

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      "/latte-api/v1/tickets",
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final Long id = ticket.getBody().id();

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/tickets/" + id,
      HttpMethod.DELETE,
      new HttpEntity<>(null, null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private AuthResponse adminCred() {
    final AuthRequest request = new AuthRequest("admin@test.in", "Admin@01");
    return authenticate(request);
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
