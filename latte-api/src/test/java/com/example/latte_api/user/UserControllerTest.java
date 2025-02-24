package com.example.latte_api.user;

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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.example.latte_api.auth.dto.AuthRequest;
import com.example.latte_api.auth.dto.AuthResponse;
import com.example.latte_api.handler.ErrorResponse;
import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.user.dto.ResetPasswordRequest;
import com.example.latte_api.user.dto.UserDto;
import com.example.latte_api.user.role.Role;
import com.example.latte_api.user.role.RoleRepository;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
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
    // remove default user
    userRepository.deleteAll();

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
    userRepository.deleteAll();
  }

  @Test
  void canEstablishConnection() {
    Assertions.assertThat(psqlContainer.isCreated()).isTrue();
    Assertions.assertThat(psqlContainer.isRunning()).isTrue();
  }

  @Test
  void shouldReturnPagedUserDto_whenAdminRequestsUserList() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<PagedEntity<UserDto>> response = testRestTemplate.exchange(
      "/latte-api/v1/users",
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<PagedEntity<UserDto>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody().getContent()).hasSize(2);
  }

  @Test
  void shouldReturnForbidden_whenUserRequestsUserList() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<PagedEntity<UserDto>> response = testRestTemplate.exchange(
      "/latte-api/v1/users",
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<PagedEntity<UserDto>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }


  @Test
  void shouldReturnForbidden_whenUserListRequestMissingAuthorizationHeader() {
    final ResponseEntity<PagedEntity<UserDto>> response = testRestTemplate.exchange(
      "/latte-api/v1/users",
      HttpMethod.GET,
      new HttpEntity<>(null),
      new ParameterizedTypeReference<PagedEntity<UserDto>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnPagedString_whenUserRequestsUserStringList() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<PagedEntity<String>> response = testRestTemplate.exchange(
      "/latte-api/v1/users/list",
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<PagedEntity<String>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody().getContent()).hasSize(2);
  }

  @Test
  void shouldReturnUserDto_ofAuthenticatedUser() {
    final UserDto expected = new UserDto("Peter", "peter@test.in", "ROLE_USER");
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<UserDto> response = testRestTemplate.exchange(
      "/latte-api/v1/users/info",
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      UserDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo(expected);
  }

  @Test
  void shouldReturnForbidden_whenUserInfoRequestMissingAuthorizationHeader() {
    final ResponseEntity<UserDto> response = testRestTemplate.exchange(
      "/latte-api/v1/users/info",
      HttpMethod.GET,
      new HttpEntity<>(null),
      UserDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnUserDto_byEmail_whenAdminRequestsUserInfo() {
    final UserDto expected = new UserDto("Peter", "peter@test.in", "ROLE_USER");
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final String user = "peter@test.in";

    final ResponseEntity<UserDto> response = testRestTemplate.exchange(
      "/latte-api/v1/users/info/" + user,
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      UserDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo(expected);
  }

  @Test
  void shouldReturnForbidden_whenUserInfoByEmailRequest_byNonAdminUser() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final String user = "peter@test.in";

    final ResponseEntity<UserDto> response = testRestTemplate.exchange(
      "/latte-api/v1/users/info/" + user,
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      UserDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnForbidden_whenUserInfoByEmailRequest_missingAuthorizationHeader() {
    final String user = "peter@test.in";

    final ResponseEntity<UserDto> response = testRestTemplate.exchange(
      "/latte-api/v1/users/info/" + user,
      HttpMethod.GET,
      new HttpEntity<>(null),
      UserDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldUpdateAuthenticatedUser_withNewDetails() {
    final UserDto expected = new UserDto("Stewie", "peter@test.in", "ROLE_USER");
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final UserDto request = new UserDto("Stewie", "peter@test.in", "ROLE_USER");

    final ResponseEntity<UserDto> response = testRestTemplate.exchange(
      "/latte-api/v1/users",
      HttpMethod.PUT,
      new HttpEntity<>(request, headers),
      UserDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo(expected);
  }

  @Test
  void shouldUpdateAuthenticatedUser_withoutChangingRole() {
    final UserDto expected = new UserDto("Stewie", "peter@test.in", "ROLE_USER");
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final UserDto request = new UserDto("Stewie", "peter@test.in", "ROLE_ADMIN");

    final ResponseEntity<UserDto> response = testRestTemplate.exchange(
      "/latte-api/v1/users",
      HttpMethod.PUT,
      new HttpEntity<>(request, headers),
      UserDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo(expected);
  }

  @Test
  void shouldReturnForbidden_whenUpdateUserRequest_missingAuthorizationHeader() {
    final UserDto request = new UserDto("Stewie", "peter@test.in", "ROLE_USER");

    final ResponseEntity<UserDto> response = testRestTemplate.exchange(
      "/latte-api/v1/users",
      HttpMethod.PUT,
      new HttpEntity<>(request, null),
      UserDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldUpdateUserByEmail_whenAdminRequestsUpdate() {
    final UserDto expected = new UserDto("Stewie", "peter@test.in", "ROLE_USER");
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final UserDto request = new UserDto("Stewie", "peter@test.in", "ROLE_USER");
    final String user = "peter@test.in";

    final ResponseEntity<UserDto> response = testRestTemplate.exchange(
      "/latte-api/v1/users/" + user,
      HttpMethod.PUT,
      new HttpEntity<>(request, headers),
      UserDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo(expected);
  }

  @Test
  void shouldUpdateUserByEmail_andChangeRole_whenAdminRequestsUpdate() {
    final UserDto expected = new UserDto("Stewie", "peter@test.in", "ROLE_ADMIN");
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final UserDto request = new UserDto("Stewie", "peter@test.in", "ROLE_ADMIN");
    final String user = "peter@test.in";

    final ResponseEntity<UserDto> response = testRestTemplate.exchange(
      "/latte-api/v1/users/" + user,
      HttpMethod.PUT,
      new HttpEntity<>(request, headers),
      UserDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo(expected);
  }

  @Test
  void shouldReturnForbidden_whenUpdateUserByEmail_byNonAdminUser() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final UserDto request = new UserDto("Stewie", "peter@test.in", "ROLE_ADMIN");
    final String user = "peter@test.in";

    final ResponseEntity<UserDto> response = testRestTemplate.exchange(
      "/latte-api/v1/users/" + user,
      HttpMethod.PUT,
      new HttpEntity<>(request, headers),
      UserDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnForbidden_whenUpdateUserByEmail_missingAuthorizationHeader() {
    final UserDto request = new UserDto("Stewie", "peter@test.in", "ROLE_ADMIN");
    final String user = "peter@test.in";

    final ResponseEntity<UserDto> response = testRestTemplate.exchange(
      "/latte-api/v1/users/" + user,
      HttpMethod.PUT,
      new HttpEntity<>(request),
      UserDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldResetPassword_forAuthenticatedUser() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResetPasswordRequest request = new ResetPasswordRequest("Updated Pass", "Updated Pass");

    final ResponseEntity<UserDto> response = testRestTemplate.exchange(
      "/latte-api/v1/users",
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      UserDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldGiveBadRequest_whenResetPassword_forAuthenticatedUser_andUpdatePasswordNotMatchConfirmPassword() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResetPasswordRequest request = new ResetPasswordRequest("Updated Pass", "UpdatePass");

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/users",
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldGiveForbidden_whenResetPassword_withMissingAuthorizationHeader() {
    final ResetPasswordRequest request = new ResetPasswordRequest("Updated Pass", "Updated Pass");

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/users",
      HttpMethod.PATCH,
      new HttpEntity<>(request, null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldResetPassword_byEmail_whenAdminRequestChanges() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResetPasswordRequest request = new ResetPasswordRequest("Updated Pass", "Updated Pass");
    final String user = "peter@test.in";

    final ResponseEntity<UserDto> response = testRestTemplate.exchange(
      "/latte-api/v1/users/" + user,
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      UserDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldGiveBadRequest_whenResetPassword_byEmail_whenAdminRequestChanges_andUpdatedPasswordNotMatchConfirmPassword() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResetPasswordRequest request = new ResetPasswordRequest("Updated Pass", "UpdatePass");
    final String user = "peter@test.in";

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/users/" + user,
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldGiveForbidden_whenResetPassword_byEmail_byNonAdmin() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResetPasswordRequest request = new ResetPasswordRequest("Updated Pass", "Updated Pass");
    final String user = "peter@test.in";

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/users/" + user,
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGiveForbidden_whenResetPassword_byEmail_missingAuthorizationHeader() {
    final ResetPasswordRequest request = new ResetPasswordRequest("Updated Pass", "Updated Pass");
    final String user = "peter@test.in";

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/users/" + user,
      HttpMethod.PATCH,
      new HttpEntity<>(request, null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldDeleteUser_byEmail_whenRequestedByAdmin() {
    final Map<String, Object> expected = Map.of("key", "peter@test.in", "deleted", true);

    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final String user = "peter@test.in";

    final ResponseEntity<Map<String, Object>> response = testRestTemplate.exchange(
      "/latte-api/v1/users/" + user,
      HttpMethod.DELETE,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<Map<String, Object>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo(expected);
  }

  @Test
  void shouldGiveForbidden_whenUserDeletedByEmail_whenRequestedByNonAdmin() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final String user = "peter@test.in";

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/users/" + user,
      HttpMethod.DELETE,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGiveForbidden_whenUserDeletedByEmail_whenRequesteMissingAuthorizationHeader() {
    final String user = "peter@test.in";

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      "/latte-api/v1/users/" + user,
      HttpMethod.DELETE,
      new HttpEntity<>(null),
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
