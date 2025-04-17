package com.example.latte_api.ticket;

import java.util.List;
import java.util.Map;

import com.example.latte_api.auth.dto.RegistrationRequest;
import com.example.latte_api.role.dto.RoleRequest;
import com.example.latte_api.role.dto.RoleResponse;
import com.example.latte_api.user.dto.UserResponse;
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
import com.example.latte_api.role.Role;
import com.example.latte_api.role.RoleRepository;
import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.ticket.dto.TicketPatchRequest;
import com.example.latte_api.ticket.dto.TicketRequest;
import com.example.latte_api.ticket.dto.TicketResponse;
import com.example.latte_api.ticket.enums.Priority;
import com.example.latte_api.ticket.enums.Status;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;

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

  private final String BASE_URI = "/latte-api/v1/tickets";

  @BeforeEach
  void setup() {
    Role user = roleRepository.findByRole("User").orElseThrow();
    Role admin = roleRepository.findByRole("Admin").orElseThrow();

    User adminUser = User.builder()
      .firstname("Admin")
      .email("admin@test.in")
      .editable(true)
      .deletable(true)
      .password(passwordEncoder.encode("password"))
      .role(admin)
      .build();

    User commonUser = User.builder()
      .firstname("User")
      .email("common@test.in")
      .editable(true)
      .deletable(true)
      .password(passwordEncoder.encode("password"))
      .role(user)
      .build();

    userRepository.saveAll(List.of(adminUser, commonUser));

    Ticket open = Ticket.builder()
      .title("T1")
      .description("description")
      .createdBy(commonUser)
      .lock(false)
      .priority(Priority.LOW)
      .status(Status.OPEN)
      .build();

    Ticket close = Ticket.builder()
      .title("T2")
      .description("description")
      .createdBy(adminUser)
      .lock(false)
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
  void shouldCreateNewTicket_withoutAssignees_forAuthenticatedUser() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest request = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null);

    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      BASE_URI,
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
    Assertions.assertThat(result.assignedTo()).isNull();
  }

  @Test
  void shouldCreateNewTicket_withAssignees_forAuthenticatedUser_HavingAuthority() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest request = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "Admin");

    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      BASE_URI,
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
  }

  @Test
  void shouldGiveForbidden_withAssignees_forAuthenticatedUser_HavingNoAuthority() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest request = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "Admin");

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
            BASE_URI,
            HttpMethod.POST,
            new HttpEntity<>(request, headers),
            ErrorResponse.class
    );
    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGiveBadRequest_onCreateTicket_withAssignees_ifAssigneeNotExists() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest request = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "Bob");

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );
    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldGiveForbidden_onCreateTicket_forRequestMissingAuthorizationHeader() {
    final TicketRequest request = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "Admin");

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI,
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
      BASE_URI,
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<PagedEntity<TicketResponse>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody().getContent()).hasSize(2);
  }

  @Test
  void shouldGiveForbidden_whenGettingTickets_withRequestMissingAuthorizationHeader() {
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI,
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
      BASE_URI + "/status/OPEN",
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<PagedEntity<TicketResponse>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody().getContent()).hasSize(1);
  }

  @Test
  void shouldGiveForbidden_whenGettingTicketsByStatus_withRequestMissingAuthorizationHeader() {
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/status/OPEN",
      HttpMethod.GET,
      new HttpEntity<>(null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGetInfo_ofTicketsOnSystem() {
    final Map<String, Integer> expected = Map.of("open_tickets", 1, "closed_tickets", 1);
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<Map<String, Integer>> response = testRestTemplate.exchange(
      BASE_URI + "/info",
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<Map<String, Integer>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo(expected);
  }

  @Test
  void shouldGiveForbidden_forTicketInfo_ofTicketsOnSystem_whenRequestMissingAuthorizationHeader() {
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/info",
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

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null);

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );


    final Long id = ticket.getBody().id();
    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
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
    Assertions.assertThat(result.assignedTo()).isNull();
  }

  @Test
  void shouldGiveEntityNotFound_onGetTicketById_forInvalidId() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final Long id = 300L;
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldGiveForbidden_onGetTicketById_forRequestMissingAuthorizationHeader() {
    final long id = 3101L;
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
      HttpMethod.GET,
      new HttpEntity<>(null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldUpdateTicketTitle_byTicketId_IfOwner() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null);

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final TicketPatchRequest request = new TicketPatchRequest("New Test", null, null, null, null);
    final Long id = ticket.getBody().id();
    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
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
    Assertions.assertThat(result.assignedTo()).isNull();
  }

  @Test
  void shouldUpdateTicketTitle_byTicketId_IfNotOwnerButHasAuthority() {
    // ticket::edit
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null);

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
            BASE_URI,
            HttpMethod.POST,
            new HttpEntity<>(ticketRequest, headers),
            TicketResponse.class
    );

    final AuthResponse newCred = adminCred();
    final HttpHeaders _headers = new HttpHeaders();
    _headers.add("Authorization", "Bearer " + newCred.accessToken());

    final TicketPatchRequest request = new TicketPatchRequest("New Test", null, null, null, null);
    final Long id = ticket.getBody().id();
    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
            BASE_URI + "/" + id,
            HttpMethod.PATCH,
            new HttpEntity<>(request,_headers),
            TicketResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    final TicketResponse result = response.getBody();
    Assertions.assertThat(result.title()).isEqualTo(request.title());
    Assertions.assertThat(result.description()).isEqualTo(ticketRequest.description());
    Assertions.assertThat(result.status()).isEqualTo(ticketRequest.status());
    Assertions.assertThat(result.priority()).isEqualTo(ticketRequest.priority());
    Assertions.assertThat(result.assignedTo()).isNull();
  }

  @Test
  void shouldReturnForbidden_onTicketUpdate_IfNotOwner_andNotHasAuthority() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null);

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
            BASE_URI,
            HttpMethod.POST,
            new HttpEntity<>(ticketRequest, headers),
            TicketResponse.class
    );

    final RoleRequest roleRequest = new RoleRequest("Temp", List.of());

    final ResponseEntity<RoleResponse> role = testRestTemplate.exchange(
            "/latte-api/v1/roles",
            HttpMethod.POST,
            new HttpEntity<>(roleRequest, headers),
            RoleResponse.class
    );

    final RegistrationRequest regRequest = new RegistrationRequest("Temp", "temp@test.in", "password", "Temp");

    final ResponseEntity<UserResponse> user = testRestTemplate.exchange(
            "/latte-api/v1/auth/sign-up",
            HttpMethod.POST,
            new HttpEntity<>(regRequest, headers),
            UserResponse.class
    );

    final AuthResponse newCred = testRestTemplate.exchange(
            "/latte-api/v1/auth/login",
            HttpMethod.POST,
            new HttpEntity<>(new AuthRequest("temp@test.in", "password"), headers),
            AuthResponse.class
    ).getBody();

    final HttpHeaders _headers = new HttpHeaders();
    _headers.add("Authorization", "Bearer " + newCred.accessToken());

    final TicketPatchRequest request = new TicketPatchRequest("New Test", null, null, null, null);
    final Long id = ticket.getBody().id();
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
            BASE_URI + "/" + id,
            HttpMethod.PATCH,
            new HttpEntity<>(request,_headers),
            ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldUpdateTicketDescription_byTicketId() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null);

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final TicketPatchRequest request = new TicketPatchRequest(null, "new-descr", null, null, null);
    final Long id = ticket.getBody().id();
    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
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
    Assertions.assertThat(result.assignedTo()).isNull();
  }

  @Test
  void shouldUpdateTicketAssignedTo_byTicketId_userHavingAuthority() {
    // ticket::assign
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null);

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final AuthResponse _cred = adminCred();

    final HttpHeaders _headers = new HttpHeaders();
    _headers.add("Authorization", "Bearer " + _cred.accessToken());

    final TicketPatchRequest request = new TicketPatchRequest(null, null, "Admin", null, null);
    final Long id = ticket.getBody().id();
    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(request, _headers),
      TicketResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    System.out.println(response.getBody());

    final TicketResponse result = response.getBody();
    Assertions.assertThat(result.title()).isEqualTo(ticketRequest.title());
    Assertions.assertThat(result.description()).isEqualTo(ticketRequest.description());
    Assertions.assertThat(result.status()).isEqualTo(ticketRequest.status());
    Assertions.assertThat(result.priority()).isEqualTo(ticketRequest.priority());
  }

  @Test
  void shouldReturnForbidden_whenTicketAssignedTo_byTicketId_userHavingNoAuthority() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null);

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
            BASE_URI,
            HttpMethod.POST,
            new HttpEntity<>(ticketRequest, headers),
            TicketResponse.class
    );

    final TicketPatchRequest request = new TicketPatchRequest(null, null, "Admin", null, null);
    final Long id = ticket.getBody().id();
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
            BASE_URI + "/" + id,
            HttpMethod.PATCH,
            new HttpEntity<>(request, headers),
            ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldUpdateTicketPriority_byTicketId() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "");

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final TicketPatchRequest request = new TicketPatchRequest(null, null, null, Priority.MEDIUM, null);
    final Long id = ticket.getBody().id();
    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
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
    Assertions.assertThat(result.assignedTo()).isNull();
  }

  @Test
  void shouldUpdateTicketStatus_byTicketId() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, "");

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final TicketPatchRequest request = new TicketPatchRequest(null, null, null, null, Status.CLOSE);
    final Long id = ticket.getBody().id();
    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
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
    Assertions.assertThat(result.assignedTo()).isNull();
  }

  @Test
  void shouldGiveNotFound_onUpdateTicket_byInvalidTicketId() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketPatchRequest request = new TicketPatchRequest("New Test", null, null, null, null);
    final long id = 300L;
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldGiveForbidden_onUpdateTicket_whenRequestMissingAuthorizationHeader() {
    final TicketPatchRequest request = new TicketPatchRequest("New Test", null, null, null, null);
    final long id = 300L;
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(request, null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldLockTicket_byTicketId_ifRequestedUserHasAuthority() {
    // ticket::lock-unlock
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null);

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final Long id = ticket.getBody().id();
    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      BASE_URI + "/lock/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(null, headers),
      TicketResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    final TicketResponse result = response.getBody();
    Assertions.assertThat(result.lock()).isTrue();
  }

  @Test
  void shouldGiveForbidden_whenTicketLock_ifRequestedUserHasNoAuthority() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null);

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final Long id = ticket.getBody().id();
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/lock/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGiveForbidden_whenTicketLock_ifRequestMissingAuthorizationHeader() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null);

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final Long id = ticket.getBody().id();
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/lock/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGiveNotFound_whenTicketLock_ifTicketNotExist() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final long id = 300L;
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/lock/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldUnlockTicket_byTicketId_ifRequestedUserHasAuthority() {
    // ticket::lock-unlock
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null);

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final Long id = ticket.getBody().id();
    final ResponseEntity<TicketResponse> response = testRestTemplate.exchange(
      BASE_URI + "/unlock/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(null, headers),
      TicketResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    final TicketResponse result = response.getBody();
    Assertions.assertThat(result.lock()).isFalse();
  }

  @Test
  void shouldGiveForbidden_whenTicketUnlock_ifRequestedUserDoNotHaveAuthority() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null);

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final Long id = ticket.getBody().id();
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/unlock/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGiveForbidden_whenTicketUnlock_ifRequestMissingAuthorizationHeader() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null);

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final Long id = ticket.getBody().id();
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/unlock/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGiveNotFound_whenTicketUnLock_ifTicketNotExist() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final long id = 300L;
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/unlock/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldDeleteTicket_byTicketId_ifRequestedBy_owner() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null);

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
            BASE_URI,
            HttpMethod.POST,
            new HttpEntity<>(ticketRequest, headers),
            TicketResponse.class
    );


    final Long id = ticket.getBody().id();
    final Map<String, Object> expected = Map.of("key", Integer.valueOf(String.valueOf(id)), "deleted", true);

    final ResponseEntity<Map<String, Object>> response = testRestTemplate.exchange(
            BASE_URI + "/" + id,
            HttpMethod.DELETE,
            new HttpEntity<>(null, headers),
            new ParameterizedTypeReference<Map<String, Object>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo(expected);
  }

  @Test
  void shouldDeleteTicket_byTicketId_ifRequestedBy_userHavingAuthority() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null);

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final AuthResponse _cred = adminCred();

    final HttpHeaders _headers = new HttpHeaders();
    _headers.add("Authorization", "Bearer " + _cred.accessToken());

    final Long id = ticket.getBody().id();
    final Map<String, Object> expected = Map.of("key", Integer.valueOf(String.valueOf(id)), "deleted", true);

    final ResponseEntity<Map<String, Object>> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
      HttpMethod.DELETE,
      new HttpEntity<>(_headers),
      new ParameterizedTypeReference<Map<String, Object>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo(expected);
  }

  @Test
  void shouldGiveNotFound_onDeleteTicketById_forInvalidId() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final long id = 300L;

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
      HttpMethod.DELETE,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldGiveForbidden_onDeleteTicketById_ifUserNotHaveAuthority() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null);

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final AuthResponse _cred = userCred();

    final HttpHeaders _headers = new HttpHeaders();
    _headers.add("Authorization", "Bearer " + _cred.accessToken());

    final Long id = ticket.getBody().id();

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
      HttpMethod.DELETE,
      new HttpEntity<>(null, _headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGiveForbidden_onDeleteTicketById_ifRequestMissingAuthorizationHeader() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final TicketRequest ticketRequest = new TicketRequest("Test", "description", Priority.LOW, Status.OPEN, null);

    final ResponseEntity<TicketResponse> ticket = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(ticketRequest, headers),
      TicketResponse.class
    );

    final Long id = ticket.getBody().id();

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
      HttpMethod.DELETE,
      new HttpEntity<>(null, null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  // Helpers

  private AuthResponse adminCred() {
    final AuthRequest request = new AuthRequest("admin@test.in", "password");
    return authenticate(request);
  }

  private AuthResponse userCred() {
    final AuthRequest request = new AuthRequest("common@test.in", "password");
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
