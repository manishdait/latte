package com.example.latte_api.role;

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
import com.example.latte_api.auth.dto.RegistrationRequest;
import com.example.latte_api.handler.ErrorResponse;
import com.example.latte_api.role.dto.RoleRequest;
import com.example.latte_api.role.dto.RoleResponse;
import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;
import com.example.latte_api.user.dto.UserResponse;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RoleControllerTest {
  @Container
  @ServiceConnection
  private final static PostgreSQLContainer<?> psqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine"));

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private TestRestTemplate testRestTemplate;

  private final String BASE_URI = "/latte-api/v1/roles";

  @BeforeEach
  void setup() {
    Role admin = roleRepository.findByRole("Admin").orElseThrow();
    Role user = roleRepository.findByRole("User").orElseThrow();

    User adminUser = User.builder()
      .firstname("Admin")
      .email("admin@test.in")
      .password(passwordEncoder.encode("password"))
      .role(admin)
      .editable(true)
      .deletable(true)
      .build();

    User commonUser = User.builder()
      .firstname("User")
      .email("common@test.in")
      .password(passwordEncoder.encode("password"))
      .role(user)
      .editable(true)
      .deletable(true)
      .build();
    userRepository.saveAll(List.of(adminUser, commonUser));
  }

  @AfterEach
  void purge() {
    userRepository.deleteAll();
  }

  @Test
  void canEstablishConnection() {
    Assertions.assertThat(psqlContainer.isCreated()).isTrue();
    Assertions.assertThat(psqlContainer.isRunning()).isTrue();
  }

  @Test
  void shouldReturn_listOfRoles() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<PagedEntity<RoleResponse>> response = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<PagedEntity<RoleResponse>>(){}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isNotNull();
  }

  @Test
  void shouldThrow_forbidden_whenMissingAuthHeader_onGetRoles() {
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.GET,
      new HttpEntity<>(null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturn_roleResponse_onRoleCreate_byUserHavingAuthority() {
    // role::create
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final RoleRequest request = new RoleRequest("Dev", List.of("ticket::create"));

    final ResponseEntity<RoleResponse> response = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(request, headers),
      RoleResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Assertions.assertThat(response.getBody()).isNotNull();

    // ensure that role get deleted after test complete
    roleRepository.deleteById(response.getBody().id());
  }

  @Test
  void shouldReturn_badRequest_onRoleCreate_forDuplicateRole_forUserHavingAuthority() {
    // role::create
    // User, Admin are added by flyway
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final RoleRequest request = new RoleRequest("Admin", List.of("ticket::create"));

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldReturn_forbidden_onRoleCreate_forUserNotHavingAuthority() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final RoleRequest request = new RoleRequest("Dev", List.of("ticket::create"));

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturn_roleResponse_onRoleUpdate_byUserHavingAuthority() {
    // role::edit
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final RoleRequest roleRequest = new RoleRequest("Dev", List.of("ticket::create"));

    RoleResponse role = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(roleRequest, headers),
      RoleResponse.class
    ).getBody();

    final long id = role.id();
    final RoleRequest request = new RoleRequest("R2", List.of("ticket::edit"));

    final ResponseEntity<RoleResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      RoleResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isNotNull();

    // ensure that role get deleted after test complete
    roleRepository.deleteById(id);
  }

  @Test
  void shouldGive_notFound_onRoleUpdate_forInvalidId() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final long id = 300L;
    final RoleRequest request = new RoleRequest("R2", List.of("ticket::edit"));

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldGive_badRequest_onRoleUpdate_ifRoleNotEditable() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final long id = 101L; // ADMIN role
    final RoleRequest request = new RoleRequest("R2", List.of("ticket::edit"));

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldGive_forbidden_onRoleUpdate_ifUserDoNotHaveAuthority() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final RoleRequest roleRequest = new RoleRequest("Dev", List.of("ticket::create"));

    RoleResponse role = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(roleRequest, headers),
      RoleResponse.class
    ).getBody();

    final AuthResponse _cred = userCred();

    final HttpHeaders _headers = new HttpHeaders();
    _headers.add("Authorization", "Bearer " + _cred.accessToken());

    final long id = role.id();
    final RoleRequest request = new RoleRequest("R2", List.of("ticket::edit"));

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
      HttpMethod.PATCH,
      new HttpEntity<>(request, _headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    // ensure that role get deleted after test complete
    roleRepository.deleteById(id);
  }

  @Test
  void shouldDelete_roleAndUpdateUserToNewRole() {
    // role::delete
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final RoleRequest roleRequest = new RoleRequest("Dev", List.of("ticket::create"));

    RoleResponse role = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(roleRequest, headers),
      RoleResponse.class
    ).getBody();

    testRestTemplate.exchange(
      "/latte-api/v1/auth/sign-up",
      HttpMethod.POST,
      new HttpEntity<>(new RegistrationRequest("Jhon", "jhon@test.in", "password", role.role()), headers),
      UserResponse.class
    );

    final long id = role.id();
    final long newId = 101L;

    final ResponseEntity<RoleResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id + "/update-to/" + newId,
      HttpMethod.DELETE,
      new HttpEntity<>(null, headers),
      RoleResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isNotNull();

    // ensure that role get deleted after test complete
    roleRepository.deleteById(id);
  }

  @Test
  void shouldGive_badRequest_onDelete_ifRoleNotDeletable() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final long id = 101L;
    final long newId = 102L;

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id + "/update-to/" + newId,
      HttpMethod.DELETE,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldGive_notFound_onDelete_ifRoleNotExists() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final long id = 300L;
    final long newId = 102L;

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id + "/update-to/" + newId,
      HttpMethod.DELETE,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldGive_forbidden_onDelete_ifUserDonNotHaveAuthority() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final RoleRequest roleRequest = new RoleRequest("Dev", List.of("ticket::create"));

    RoleResponse role = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(roleRequest, headers),
      RoleResponse.class
    ).getBody();

    final AuthResponse _cred = userCred();

    final HttpHeaders _headers = new HttpHeaders();
    _headers.add("Authorization", "Bearer " + _cred.accessToken());

    final long id = role.id();
    final long newId = 101L;

    final ResponseEntity<RoleResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + id + "/update-to/" + newId,
      HttpMethod.DELETE,
      new HttpEntity<>(null, _headers),
      RoleResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    // ensure that role get deleted after test complete
    roleRepository.deleteById(id);
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
