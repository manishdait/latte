package com.example.latte_api.role;

import static com.example.latte_api.TestUtils.TEST_AUTHORITY_READ;
import static com.example.latte_api.TestUtils.createAuthority;
import static com.example.latte_api.TestUtils.createRole;

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

import com.example.latte_api.role.authority.Authority;
import com.example.latte_api.role.authority.AuthorityRepository;

/*
 * Role Repository Test
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
  "spring.flyway.enabled=false", 
  "spring.jpa.hibernate.ddl-auto=update"
})
public class RoleRepositoryTest {
  @Container
  @ServiceConnection
  private final static PostgreSQLContainer<?> psqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine")); 

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private AuthorityRepository authorityRepository;

  @BeforeEach
  void setup() {
    Authority authority = createAuthority(TEST_AUTHORITY_READ);
    authorityRepository.save(authority);

    Role role = createRole("USER", authority);
    roleRepository.save(role);
  }

  @AfterEach
  void purge() {
    roleRepository.deleteAll();
    authorityRepository.deleteAll();
  }

  @Test
  void canEstablishConnection() {
    Assertions.assertThat(psqlContainer.isCreated()).isTrue();
    Assertions.assertThat(psqlContainer.isRunning()).isTrue();
  }

  @Test
  void shouldReturn_roleOptional_forValidRole() {
    final String role = "USER";
    final Optional<Role> result = roleRepository.findByRole(role);
    Assertions.assertThat(result).isPresent();
  }

  @Test
  void shouldReturn_emptyOptional_forInalidRole() {
    final String role = "ADMIN";
    final Optional<Role> result = roleRepository.findByRole(role);
    Assertions.assertThat(result).isEmpty();
  }
}
