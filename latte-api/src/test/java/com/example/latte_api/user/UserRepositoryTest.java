package com.example.latte_api.user;

import java.util.Optional;

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

import com.example.latte_api.user.role.Role;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class UserRepositoryTest {
  @Container
  @ServiceConnection
  private final static PostgreSQLContainer<?> psqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine")); 

  @Autowired
  private UserRepository userRepository;

  private User user = User.builder()
    .firstname("Peter")
    .email("peter@test.in")
    .password("Peter01")
    .role(Role.builder().id(101L).role("ROLE_USER").build())
    .build();

  @BeforeEach
  void setup() {
    userRepository.save(user);
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
  void shouldReturn_userOptional_forValidEmail() {
    final String email = "peter@test.in";
    final Optional<User> result = userRepository.findByEmail(email);

    Assertions.assertThat(result).isPresent();
  }

  @Test
  void shouldReturn_emptyOptional_forInvalidEmail() {
    final String email = "louis@test.in";
    final Optional<User> result = userRepository.findByEmail(email);

    Assertions.assertThat(result).isEmpty();
  }

  @Test
  void shouldReturn_userOptional_forValidFirstname() {
    final String firstname = "Peter";
    final Optional<User> result = userRepository.findByFirstname(firstname);

    Assertions.assertThat(result).isPresent();
  }

  @Test
  void shouldReturn_emptyOptional_forInvalidFirstname() {
    final String firstname = "Louis";
    final Optional<User> result = userRepository.findByFirstname(firstname);

    Assertions.assertThat(result).isEmpty();
  }

  @Test
  void shouldReturn_userOptional_ifEmailOrFirstname_isValid() {
    final String firstname = "Peter";
    final String email = "stewie@test.in";
    final Optional<User> result = userRepository.findByEmailOrFirstname(email, firstname);

    Assertions.assertThat(result).isPresent();
  }

  @Test
  void shouldReturn_emptyOptional_ifEmailAndFirstname_isInvalid() {
    final String firstname = "Louis";
    final String email = "louis@test.in";
    final Optional<User> result = userRepository.findByEmailOrFirstname(email, firstname);

    Assertions.assertThat(result).isEmpty();
  }
}
