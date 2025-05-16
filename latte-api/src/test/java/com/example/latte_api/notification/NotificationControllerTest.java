package com.example.latte_api.notification;

import java.time.Instant;
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
import com.example.latte_api.role.Role;
import com.example.latte_api.role.RoleRepository;
import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class NotificationControllerTest {
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
  private NotificationRepository notificationRepository;

  @Autowired
  private TestRestTemplate testRestTemplate;

  private final String BASE_URI = "/latte-api/v1/notifications";

  @BeforeEach
  void setup() {
    Role user = roleRepository.findByRole("User").orElseThrow();
    
    User u1 = User.builder()
      .firstname("Admin")
      .email("admin@test.in")
      .password(passwordEncoder.encode("password"))
      .role(user)
      .build();

    User u2 = User.builder()
      .firstname("User")
      .email("common@test.in")
      .password(passwordEncoder.encode("password"))
      .role(user)
      .build();
    
    userRepository.saveAll(List.of(u1,u2));

    notificationRepository.save(Notification.builder().message("message1").user(u1).timestamp(Instant.now()).build());
  }

  @AfterEach
  void purge() {
    notificationRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void canEstablishConnection() {
    Assertions.assertThat(psqlContainer.isCreated()).isTrue();
    Assertions.assertThat(psqlContainer.isRunning()).isTrue();
  }

  @Test
  void shouldReturn_pageOfNotification_forUser() {
    AuthResponse cred = u1Cred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<PagedEntity<NotificationDto>> response = testRestTemplate.exchange(
      BASE_URI + "?page=0&size=1",
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<PagedEntity<NotificationDto>>(){}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody().getContent()).hasSize(1);
  }

  @Test
  void shouldDelete_notification_forGivenId() {
    final User user = userRepository.findByEmail("common@test.in").orElseThrow();

    final Notification notification = notificationRepository.save(
      Notification.builder()
        .message("message1")
        .user(user)
        .timestamp(Instant.now())
        .build()
    );
    
    AuthResponse cred = u2Cred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final Long id = notification.getId();
    final ResponseEntity<Map<String, Boolean>> response = testRestTemplate.exchange(
      BASE_URI + "/" + id,
      HttpMethod.DELETE,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<Map<String, Boolean>>(){}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }


  private AuthResponse u2Cred() {
    final AuthRequest request = new AuthRequest("common@test.in", "password");
    return authenticate(request);
  }

  private AuthResponse u1Cred() {
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
