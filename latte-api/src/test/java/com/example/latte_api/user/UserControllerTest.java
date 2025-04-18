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
import com.example.latte_api.user.dto.ResetPasswordRequest;
import com.example.latte_api.user.dto.UserRequest;
import com.example.latte_api.user.dto.UserResponse;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
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

  private final String BASE_URI = "/latte-api/v1/users";

  @BeforeEach
  void setup() {
    Role user = roleRepository.findByRole("User").orElseThrow();
    Role admin = roleRepository.findByRole("Admin").orElseThrow();

    User superUser = User.builder()
      .firstname("SuperUser")
      .email("superuser@test.in")
      .password(passwordEncoder.encode("password"))
      .editable(false)
      .deletable(false)
      .role(admin)
      .build();

    User adminUser = User.builder()
      .firstname("Admin")
      .email("admin@test.in")
      .password(passwordEncoder.encode("password"))
      .editable(true)
      .deletable(true)
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

    userRepository.saveAll(List.of(superUser, adminUser, commonUser));
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
  void shouldReturnPagedUserResponse_whenRequestsUserList_byUserHavingProperAuthority() { 
    // user::create || user::edit || user::delete || user::reset-password
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<PagedEntity<UserResponse>> response = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<PagedEntity<UserResponse>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody().getContent()).hasSize(3);
  }

  @Test
  void shouldReturnForbidden_whenRequestsUserList_donotHaveProperAuhtority() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnForbidden_whenUserListRequestMissingAuthorizationHeader() {
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.GET,
      new HttpEntity<>(null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnPagedUser_asStringList() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<PagedEntity<String>> response = testRestTemplate.exchange(
      BASE_URI + "/list",
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<PagedEntity<String>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody().getContent()).hasSize(3);
  }

  @Test
  void shouldReturnForbidden_whenPagedUser_asStringList_requestMissingAuthHeaders() {
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/list",
      HttpMethod.GET,
      new HttpEntity<>(null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnUserResponse_ofAuthenticatedUser() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<UserResponse> response = testRestTemplate.exchange(
      BASE_URI + "/info",
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      UserResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    final UserResponse user = response.getBody();
    Assertions.assertThat(user.firstname()).isEqualTo("User");
    Assertions.assertThat(user.email()).isEqualTo("common@test.in");
    Assertions.assertThat(user.role().role()).isEqualTo("User");
    Assertions.assertThat(user.editable()).isEqualTo(true);
    Assertions.assertThat(user.deletable()).isEqualTo(true);
  }

  @Test
  void shouldReturnForbidden_whenUserInfo_requestMissingAuthorizationHeader() {
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
     BASE_URI + "/info",
      HttpMethod.GET,
      new HttpEntity<>(null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnUserResponse_byEmail_whenRequestsUserInfo_byUserHavingProperAuthorities() {
    // user::create || user::edit || user::delete || user::reset-password
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final String _user = "common@test.in";

    final ResponseEntity<UserResponse> response = testRestTemplate.exchange(
      BASE_URI + "/info/" + _user,
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      UserResponse.class
    );

    final UserResponse user = response.getBody();
    Assertions.assertThat(user.firstname()).isEqualTo("User");
    Assertions.assertThat(user.email()).isEqualTo("common@test.in");
    Assertions.assertThat(user.role().role()).isEqualTo("User");
    Assertions.assertThat(user.editable()).isEqualTo(true);
    Assertions.assertThat(user.deletable()).isEqualTo(true);
  }

  @Test
  void shouldReturnForbidden_whenUserInfoByEmailRequest_byUserNotHavingAuthorities() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final String _user = "common@test.in";

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/info/" + _user,
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnForbidden_whenUserInfoByEmailRequest_missingAuthorizationHeader() {
    final String _user = "common@test.in";

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/info/" + _user,
      HttpMethod.GET,
      new HttpEntity<>(null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldUpdateAuthenticatedUser_withNewDetails() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final UserRequest request = new UserRequest("Stewie", "stewie@test.in", "User");

    final ResponseEntity<UserResponse> response = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.PUT,
      new HttpEntity<>(request, headers),
      UserResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    final UserResponse user = response.getBody();
    Assertions.assertThat(user.firstname()).isEqualTo("Stewie");
    Assertions.assertThat(user.email()).isEqualTo("stewie@test.in");
    Assertions.assertThat(user.role().role()).isEqualTo("User");
    Assertions.assertThat(user.editable()).isEqualTo(true);
    Assertions.assertThat(user.deletable()).isEqualTo(true);
  }

  @Test
  void shouldUpdateAuthenticatedUser_verifyThat_itNotChangeRole() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final UserRequest request = new UserRequest("Stewie", "stewie@test.in", "Admin");

    final ResponseEntity<UserResponse> response = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.PUT,
      new HttpEntity<>(request, headers),
      UserResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    final UserResponse user = response.getBody();
    Assertions.assertThat(user.firstname()).isEqualTo("Stewie");
    Assertions.assertThat(user.email()).isEqualTo("stewie@test.in");
    Assertions.assertThat(user.role().role()).isEqualTo("User");
    Assertions.assertThat(user.editable()).isEqualTo(true);
    Assertions.assertThat(user.deletable()).isEqualTo(true);
  }

  @Test
  void shouldReturnForbidden_whenUpdateUserRequest_missingAuthorizationHeader() {
    final UserRequest request = new UserRequest("Stewie", "stewie@test.in", "User");

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.PUT,
      new HttpEntity<>(request, null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnBadRequest_whenUpdateUserRequest_ifUserIsNotEditable() {
    final AuthResponse cred = superCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final UserRequest request = new UserRequest("Stewie", "stewie@test.in", "Admin");

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.PUT,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldUpdateUserByEmail_whenRequestsUpdate_byUserHavingAuthorities() {
    // user::edit
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final UserRequest request = new UserRequest("Stewie", "stewie@test.in", "User");
    final String _user = "common@test.in";

    final ResponseEntity<UserResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + _user,
      HttpMethod.PUT,
      new HttpEntity<>(request, headers),
      UserResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final UserResponse user = response.getBody();

    Assertions.assertThat(user.firstname()).isEqualTo("Stewie");
    Assertions.assertThat(user.email()).isEqualTo("stewie@test.in");
    Assertions.assertThat(user.role().role()).isEqualTo("User");
    Assertions.assertThat(user.editable()).isEqualTo(true);
    Assertions.assertThat(user.deletable()).isEqualTo(true);
  }

  @Test
  void shouldUpdateUserByEmail_andChangeRole_whenRequestsUpdate_byUserHavingAuthorities() {
    // user::edit
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final UserRequest request = new UserRequest("Stewie", "stewie@test.in", "Admin");
    final String _user = "common@test.in";

    final ResponseEntity<UserResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + _user,
      HttpMethod.PUT,
      new HttpEntity<>(request, headers),
      UserResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    final UserResponse user = response.getBody();
    Assertions.assertThat(user.firstname()).isEqualTo("Stewie");
    Assertions.assertThat(user.email()).isEqualTo("stewie@test.in");
    Assertions.assertThat(user.role().role()).isEqualTo("Admin");
    Assertions.assertThat(user.editable()).isEqualTo(true);
    Assertions.assertThat(user.deletable()).isEqualTo(true);
  }

  @Test
  void shouldReturnForbidden_whenUpdateUserByEmail_forUserNotHavingAuthorities() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final UserRequest request = new UserRequest("Stewie", "stewie@test.in", "Admin");
    final String _user = "common@test.in";

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + _user,
      HttpMethod.PUT,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnForbidden_whenUpdateUserByEmail_missingAuthorizationHeader() {
    final UserRequest request = new UserRequest("Stewie", "stewie@test.in", "Admin");
    final String _user = "common@test.in";

    final ResponseEntity<UserResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + _user,
      HttpMethod.PUT,
      new HttpEntity<>(request),
      UserResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldResetPassword_forAuthenticatedUser() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResetPasswordRequest request = new ResetPasswordRequest("Updated Pass", "Updated Pass");

    final ResponseEntity<UserResponse> response = testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      UserResponse.class
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
      BASE_URI,
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
      BASE_URI,
      HttpMethod.PATCH,
      new HttpEntity<>(request, null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldResetPassword_byEmail_whenuserRequestChanges_forUserHavingAuthorities() {
    // user::reset-password
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResetPasswordRequest request = new ResetPasswordRequest("Updated Pass", "Updated Pass");
    final String _user = "common@test.in";

    final ResponseEntity<UserResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + _user,
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      UserResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldGiveBadRequest_whenResetPassword_byEmail_whenRequestChanges_andUpdatedPasswordNotMatchConfirmPassword_forValidAuthorityUser() {
    // user::reset-password
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResetPasswordRequest request = new ResetPasswordRequest("Updated Pass", "UpdatePass");
    final String _user = "common@test.in";

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + _user,
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldGiveForbidden_whenResetPassword_byEmail_forUserNotHavingAuthorities() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResetPasswordRequest request = new ResetPasswordRequest("Updated Pass", "Updated Pass");
    final String _user = "common@test.in";

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + _user,
      HttpMethod.PATCH,
      new HttpEntity<>(request, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGiveForbidden_whenResetPassword_byEmail_missingAuthorizationHeader() {
    final ResetPasswordRequest request = new ResetPasswordRequest("Updated Pass", "Updated Pass");
    final String user = "common@test.in";

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + user,
      HttpMethod.PATCH,
      new HttpEntity<>(request, null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldDeleteUser_byEmail_whenRequested_byUserHavingAuthority() {
    // user::delete
    final Map<String, Object> expected = Map.of("key", "common@test.in", "deleted", true);

    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final String _user = "common@test.in";

    final ResponseEntity<Map<String, Object>> response = testRestTemplate.exchange(
      BASE_URI + "/" + _user,
      HttpMethod.DELETE,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<Map<String, Object>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo(expected);
  }

  @Test
  void shouldGiveForbidden_whenUserDeletedByEmail_whenRequested_byUserNotHavingAuthority() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final String _user = "common@test.in";

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + _user,
      HttpMethod.DELETE,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGiveForbidden_whenUserDeletedByEmail_whenRequesteMissingAuthorizationHeader() {
    final String _user = "common@test.in";

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + _user,
      HttpMethod.DELETE,
      new HttpEntity<>(null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGiveBadRequest_whenUserDeletedByEmail_whenUserIsNotDeletable() {
    final AuthResponse cred = superCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final String _user = "superuser@test.in";

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/" + _user,
      HttpMethod.DELETE,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldReturn_userCount_whenRequest_byUserHavingAuthority() {
    // user::create || user::edit || user::delete || user::reset-password
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<Map<String, Long>> response = testRestTemplate.exchange(
      BASE_URI + "/count",
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<Map<String, Long>>() {}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo(Map.of("user_count", 3L));
  }

  @Test
  void shouldGiveForbidden_whenGettingUserCount_whenRequested_byUserNotHavingAuhtorities() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/count",
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGiveForbidden_whenGettingUserCount_whenRequestMissingAuthorizationHeader() {
    final ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
      BASE_URI + "/count",
      HttpMethod.GET,
      new HttpEntity<>(null),
      ErrorResponse.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  // Helpers

  private AuthResponse superCred() {
    final AuthRequest request = new AuthRequest("superuser@test.in", "password");
    return authenticate(request);
  }

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
