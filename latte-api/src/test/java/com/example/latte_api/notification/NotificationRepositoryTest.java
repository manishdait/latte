package com.example.latte_api.notification;

import java.time.Instant;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.example.latte_api.role.Role;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class NotificationRepositoryTest {
  @Container
  @ServiceConnection
  private final static PostgreSQLContainer<?> psqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine"));

  @Autowired
  private UserRepository userRepository;
  
  @Autowired
  private NotificationRepository notificationRepository;

  private User user;

  @BeforeEach
  void setup() {
    user = userRepository.save(
      User.builder()
        .firstname("Peter")
        .email("peter@test.in")
        .password("password")
        .role(Role.builder().id(101L).role("ROLE_USER").build())
        .build()
    );

    Notification notification = Notification.builder()
      .message("Notification")
      .user(user)
      .timestamp(Instant.now())
      .build();

    notificationRepository.save(notification);
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
  void shouldReturn_listOfNotification_whenFindByUser() {
    final List<Notification> result = notificationRepository.findByUser(user);
    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result).isNotEmpty();
  }

  @Test
  void shouldReturn_emptyList_whenFindByUser_userNotHaveNotifi() {
    User newUser = User.builder()
      .id(102L)
      .firstname("Louis")
      .email("louis@test.in")
      .password("password")
      .role(Role.builder().id(101L).role("ROLE_USER").build())
      .build();
    final List<Notification> result = notificationRepository.findByUser(newUser);
    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result).isEmpty();
  }
}
