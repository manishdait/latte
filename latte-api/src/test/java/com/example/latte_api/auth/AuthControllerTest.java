package com.example.latte_api.auth;

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
import com.example.latte_api.auth.dto.RegistrationRequest;
import com.example.latte_api.handler.ErrorResponse;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;
import com.example.latte_api.user.role.Role;
import com.example.latte_api.user.role.RoleRepository;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthControllerTest {
  @Container
  @ServiceConnection
  private final static PostgreSQLContainer<?> psqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine"));

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

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
      .editable(true)
      .deletable(false)
      .build();

    User peter = User.builder()
      .firstname("Peter")
      .email("peter@test.in")
      .password(passwordEncoder.encode("Peter@01"))
      .role(user)
      .editable(true)
      .deletable(true)
      .build();

    userRepository.saveAll(List.of(jhon, peter));
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
  void should_createNewUser_whenUserDoesNotExist() {
    final Map<String, Boolean> expected = Map.of("user_created", true);

    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());
 
    final RegistrationRequest request = new RegistrationRequest("Jhon", "jhon@gmail.com", "Jhon@01", "ROLE_USER");

    final ResponseEntity<Map<String, Boolean>> response = testRestTemplate.exchange(
      "/latte-api/v1/auth/sign-up",
      HttpMethod.POST,
      new HttpEntity<>(request, headers),
      new ParameterizedTypeReference<Map<String, Boolean>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Assertions.assertThat(response.getBody()).isEqualTo(expected);
  }

  @Test
  void shouldReturnBadRequest_whenRegisteringUser_withExistingFirstname() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());
 
    final RegistrationRequest request = new RegistrationRequest("Peter", "jhon@gmail.com", "Jhon@01", "ROLE_USER");

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/auth/sign-up",
      HttpMethod.POST,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    Assertions.assertThat(response.getBody().error()).isEqualTo("Duplicate User");
  }


  @Test
  void shouldReturnBadRequest_whenRegisteringUser_withExistingEmail() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());
 
    final RegistrationRequest request = new RegistrationRequest("Jhon", "peter@test.in", "Jhon@01", "ROLE_USER");

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/auth/sign-up",
      HttpMethod.POST,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    Assertions.assertThat(response.getBody().error()).isEqualTo("Duplicate User");
  }

  @Test
  void shouldReturnBadRequest_whenRegisteringUser_withExistingFirstnameAndEmail() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());
 
    final RegistrationRequest request = new RegistrationRequest("Peter", "peter@test.in", "Jhon@01", "ROLE_USER");

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/auth/sign-up",
      HttpMethod.POST,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    Assertions.assertThat(response.getBody().error()).isEqualTo("Duplicate User");
  }

  @Test
  void shouldReturnForbidden_whenRegistrationRequest_missingAuthorizationHeader() {
    final RegistrationRequest request = new RegistrationRequest("Jhon", "jhon@test.in", "Jhon@01", "ROLE_USER");

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/auth/sign-up",
      HttpMethod.POST,
      new HttpEntity<>(request),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnForbidden_onRegisterUser_whenAuthenticatedUserIsNotAdmin() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final RegistrationRequest request = new RegistrationRequest("Jhon", "jhon@test.in", "Jhon@01", "ROLE_USER");

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/auth/sign-up",
      HttpMethod.POST,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnOk_withAuthResponse_onSuccessfulLogin() {
    final AuthRequest request = new AuthRequest("admin@test.in", "Admin@01");

    final ResponseEntity<AuthResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/auth/login",
      HttpMethod.POST,
      new HttpEntity<>(request),
      AuthResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody().email()).isEqualTo(request.email());
    Assertions.assertThat(response.getBody().accessToken()).isNotEmpty();
    Assertions.assertThat(response.getBody().refreshToken()).isNotEmpty();
  }

  @Test
  void shouldReturnForbidden_onLogin_withInvalidCredentials() {
    final AuthRequest request = new AuthRequest("admin@test.in", "Peter@01");

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/auth/login",
      HttpMethod.POST,
      new HttpEntity<>(request),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnForbidden_onLogin_whenUserNotExist() {
    final AuthRequest request = new AuthRequest("jhon@test.in", "Jhon@01");

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/auth/login",
      HttpMethod.POST,
      new HttpEntity<>(request),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnOk_withNewAccessToken_onValidRefreshToken() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.refreshToken());
 
    final ResponseEntity<AuthResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/auth/refresh",
      HttpMethod.POST,
      new HttpEntity<>(null, headers),
      AuthResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody().email()).isEqualTo(cred.email());
    Assertions.assertThat(response.getBody().refreshToken()).isEqualTo(cred.refreshToken());
    Assertions.assertThat(response.getBody().accessToken()).isNotEmpty();
  }

  @Test
  void shouldReturnUnauthorized_onRefreshToken_withInvalidToken() {
    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + "random-token");
 
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/auth/refresh",
      HttpMethod.POST,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldReturnInternalServerError_onRefreshTokenRequest_missingAuthorizationHeader() {
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/auth/refresh",
      HttpMethod.POST,
      new HttpEntity<>(null, null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void shouldReturnOk_onVerify_withValidAccessToken() {
    final Map<String, Boolean> expected = Map.of("success", true);

    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());
 
    final ResponseEntity<Map<String, Boolean>> response = testRestTemplate.exchange(
      "/latte-api/v1/auth/verify",
      HttpMethod.POST,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<Map<String, Boolean>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo(expected);
  }

  @Test
  void shouldReturnUnauthorized_onVerify_withInvalidAccessToken() {
    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer acess-token");
 
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/auth/verify",
      HttpMethod.POST,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldReturnForbidden_onVerify_whenRequestMissingAuthorizationHeader() {
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/auth/verify",
      HttpMethod.POST,
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
