package com.example.latte_api.role.authority;

import static com.example.latte_api.TestUtils.TEST_AUTHORITY_READ;
import static com.example.latte_api.TestUtils.createAuthority;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/*
 * Authority Repository Test
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
  "spring.flyway.enabled=false", 
  "spring.jpa.hibernate.ddl-auto=update"
})
public class AuthorityRepositoryTest {
  @Container
  @ServiceConnection
  private final static PostgreSQLContainer<?> psqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine"));

  @Autowired
  private AuthorityRepository authorityRepository;

  @BeforeEach
  void setup() {
    authorityRepository.save(createAuthority(TEST_AUTHORITY_READ));
  }

  @AfterEach
  void purge() {
    authorityRepository.deleteAll();
  }

  @Test
  void canEstablishConnection() {
    Assertions.assertThat(psqlContainer.isCreated()).isTrue();
    Assertions.assertThat(psqlContainer.isRunning()).isTrue();
  }

  @Test
  void shouldReturn_authorityOptional_forValidAuthority() {
    final String authority = TEST_AUTHORITY_READ;
    final Optional<Authority> result = authorityRepository.findByAuthorityIgnoreCase(authority);

    Assertions.assertThat(result).isPresent();
  }

  @Test
  void shouldReturn_authorityOptional_forValidAuthority_DifferentCases() {
    final String authority = TEST_AUTHORITY_READ.toLowerCase();
    final Optional<Authority> result = authorityRepository.findByAuthorityIgnoreCase(authority);

    Assertions.assertThat(result).isPresent();
  }

  @Test
  void shouldReturn_emptyOptional_forInvalidAuthority() {
    final String authority = "unknown::authority";
    final Optional<Authority> result = authorityRepository.findByAuthorityIgnoreCase(authority);

    Assertions.assertThat(result).isEmpty();
  }
}
