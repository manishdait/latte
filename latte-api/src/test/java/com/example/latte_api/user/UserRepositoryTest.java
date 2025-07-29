package com.example.latte_api.user;

import static com.example.latte_api.TestUtils.TEST_FIRSTNAME_PETER;
import static com.example.latte_api.TestUtils.TEST_FIRSTNAME_LOUIS;
import static com.example.latte_api.TestUtils.TEST_EMAIL_PETER;
import static com.example.latte_api.TestUtils.TEST_EMAIL_LOUIS;
import static com.example.latte_api.TestUtils.createTestUserPeter;

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

/*
 * User Repository Test
 */
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

  @BeforeEach
  void setup() {
    userRepository.save(createTestUserPeter());
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
    final String email = TEST_EMAIL_PETER;
    final Optional<User> result = userRepository.findByEmail(email);

    Assertions.assertThat(result).isPresent();
  }

  @Test
  void shouldReturn_emptyOptional_forInvalidEmail() {
    final String email = TEST_EMAIL_LOUIS;
    final Optional<User> result = userRepository.findByEmail(email);

    Assertions.assertThat(result).isEmpty();
  }

  @Test
  void shouldReturn_userOptional_forValidFirstname() {
    final String firstname = TEST_FIRSTNAME_PETER;
    final Optional<User> result = userRepository.findByFirstname(firstname);

    Assertions.assertThat(result).isPresent();
  }

  @Test
  void shouldReturn_emptyOptional_forInvalidFirstname() {
    final String firstname = TEST_FIRSTNAME_LOUIS;
    final Optional<User> result = userRepository.findByFirstname(firstname);

    Assertions.assertThat(result).isEmpty();
  }

  @Test
  void shouldReturn_userOptional_ifEmailOrFirstname_isValid() {
    final String firstname = TEST_FIRSTNAME_PETER;
    final String email = TEST_EMAIL_LOUIS;
    final Optional<User> result = userRepository.findByEmailOrFirstname(email, firstname);

    Assertions.assertThat(result).isPresent();
  }

  @Test
  void shouldReturn_emptyOptional_ifEmailAndFirstname_isInvalid() {
    final String firstname = TEST_FIRSTNAME_LOUIS;
    final String email = TEST_EMAIL_LOUIS;
    final Optional<User> result = userRepository.findByEmailOrFirstname(email, firstname);

    Assertions.assertThat(result).isEmpty();
  }
}
