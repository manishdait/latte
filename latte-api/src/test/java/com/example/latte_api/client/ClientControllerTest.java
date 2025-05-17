package com.example.latte_api.client;

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
import com.example.latte_api.client.dto.ClientRequest;
import com.example.latte_api.client.dto.ClientResponse;
import com.example.latte_api.handler.ErrorResponse;
import com.example.latte_api.role.Role;
import com.example.latte_api.role.RoleRepository;
import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ClientControllerTest {
  @Container
  @ServiceConnection
  private static final PostgreSQLContainer<?> psqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine"));

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private ClientRepository clientRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private TestRestTemplate testRestTemplate;

  private final String BASE_URI = "/latte-api/v1/clients";

  @BeforeEach
  void setup() {
    Role user = roleRepository.findByRole("User").orElseThrow();
    Role admin = roleRepository.findByRole("Admin").orElseThrow();

    User adminUser = User.builder()
      .firstname("Admin")
      .email("admin@test.in")
      .editable(true)
      .deletable(true)
      .password(passwordEncoder.encode("password"))
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

    userRepository.saveAll(List.of(adminUser, commonUser));

    Client client = Client.builder()
      .name("Client 1")
      .email("client@test.in")
      .phone("+919090909090")
      .build();
    clientRepository.save(client);
  }

  @AfterEach
  void purge() {
    userRepository.deleteAll();
    clientRepository.deleteAll();
  }

  @Test
  void shouldGet_pagedClientResponse() {
    final AuthResponse cred = userCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<PagedEntity<ClientResponse>> result =  testRestTemplate.exchange(
      BASE_URI + "?page=0&size=1",
      HttpMethod.GET,
      new HttpEntity<>(null,headers),
      new ParameterizedTypeReference<PagedEntity<ClientResponse>>() {}
    );

    Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldGiveForbidden_whenGetClinet_ifRequestMissingAuthHeaders() {
    final ResponseEntity<ErrorResponse> result =  testRestTemplate.exchange(
      BASE_URI + "?page=0&size=1",
      HttpMethod.GET,
      new HttpEntity<>(null),
      ErrorResponse.class
    );

    Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGet_ClientResponse() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());
    
    final ClientRequest request = new ClientRequest("Bob", "bob@test.in", "+917878787878");

    final ClientResponse client =  testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(request,headers),
      ClientResponse.class
    ).getBody();

    final ResponseEntity<ClientResponse> result =  testRestTemplate.exchange(
      BASE_URI + "/" + client.id(),
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      ClientResponse.class
    );

    Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldGiveNotfound_whenGetClient_forInvalidId() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final Long id = 400l;

    final ResponseEntity<ErrorResponse> result =  testRestTemplate.exchange(
      BASE_URI + "/" + id,
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      ErrorResponse.class
    );

    Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
  
  @Test
  void shouldGiveForbidden_whenRetClient_ifRequestMissingAuthHeader() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());
    
    final ClientRequest request = new ClientRequest("Bob", "bob@test.in", "+917878787878");

    final ClientResponse client =  testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(request,headers),
      ClientResponse.class
    ).getBody();

    final ResponseEntity<ErrorResponse> result =  testRestTemplate.exchange(
      BASE_URI + "/" + client.id(),
      HttpMethod.GET,
      new HttpEntity<>(null),
      ErrorResponse.class
    );

    Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldCreate_newClient_forValidUser() {
    final AuthResponse cred = adminCred();

    final ClientRequest request = new ClientRequest("Bob", "bob@test.in", "+917878787878");
    
    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<ClientResponse> result =  testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(request,headers),
      ClientResponse.class
    );

    Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void shouldGiveForbidden_onCreateNewClient_forUnauthorizeUser() {
    final AuthResponse cred = userCred();

    final ClientRequest request = new ClientRequest("Bob", "bob@test.in", "+917878787878");
    
    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ResponseEntity<ErrorResponse> result =  testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(request,headers),
      ErrorResponse.class
    );

    Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGiveForbidden_onCreateNewClient_forMissingAuthHeader() {
    final ClientRequest request = new ClientRequest("Bob", "bob@test.in", "+917878787878");

    final ResponseEntity<ErrorResponse> result =  testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(request),
      ErrorResponse.class
    );

    Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturn_clientResponse_whenUpdated() {
    final AuthResponse cred = adminCred();

    final ClientRequest request1 = new ClientRequest("Bob", "bob@test.in", "+917878787878");
    
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ClientResponse client =  testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(request1,headers),
      ClientResponse.class
    ).getBody();

    final ClientRequest request2 = new ClientRequest("NewBob", "newbob@test.in", "+917878787878");
    final ResponseEntity<ClientResponse> result =  testRestTemplate.exchange(
      BASE_URI + "/" + client.id(),
      HttpMethod.PUT,
      new HttpEntity<>(request2,headers),
      ClientResponse.class
    );

    Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldGiveNotfound_whenUpdated_invalidId() {
    final AuthResponse cred = adminCred();
    
    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ClientRequest request = new ClientRequest("NewBob", "newbob@test.in", "+917878787878");
    final Long id = 400L;

    final ResponseEntity<ErrorResponse> result =  testRestTemplate.exchange(
      BASE_URI + "/" + id,
      HttpMethod.PUT,
      new HttpEntity<>(request,headers),
      ErrorResponse.class
    );

    Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldGiveForbidden_whenUpdated_forUnauthorizeUser() {
    final AuthResponse cred1 = adminCred();

    final ClientRequest request1 = new ClientRequest("Bob", "bob@test.in", "+917878787878");
    
    final HttpHeaders headers1 = new HttpHeaders();
    headers1.add("Authorization", "Bearer " + cred1.accessToken());

    final ClientResponse client =  testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(request1,headers1),
      ClientResponse.class
    ).getBody();

    final AuthResponse cred2 = userCred();
    final HttpHeaders headers2 = new HttpHeaders();

    headers2.add("Authorization", "Bearer " + cred2.accessToken());
    final ClientRequest request2 = new ClientRequest("NewBob", "newbob@test.in", "+917878787878");
    final ResponseEntity<ErrorResponse> result =  testRestTemplate.exchange(
      BASE_URI + "/" + client.id(),
      HttpMethod.PUT,
      new HttpEntity<>(request2,headers2),
      ErrorResponse.class
    );

    Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGiveForbiden_whenUpdated_missignAuthHeader() {
    final AuthResponse cred = adminCred();

    final ClientRequest request1 = new ClientRequest("Bob", "bob@test.in", "+917878787878");
    
    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ClientResponse client =  testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(request1,headers),
      ClientResponse.class
    ).getBody();

    final ClientRequest request2 = new ClientRequest("NewBob", "newbob@test.in", "+917878787878");
    final ResponseEntity<ErrorResponse> result =  testRestTemplate.exchange(
      BASE_URI + "/" + client.id(),
      HttpMethod.PUT,
      new HttpEntity<>(request2),
      ErrorResponse.class
    );

    Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldDeleteClient_withId_whenRequestedByUser() {
    final AuthResponse cred = adminCred();

    final ClientRequest request = new ClientRequest("Bob", "bob@test.in", "+917878787878");
    
    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final ClientResponse client =  testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(request,headers),
      ClientResponse.class
    ).getBody();

    final ResponseEntity<Map<String, Boolean>> result =  testRestTemplate.exchange(
      BASE_URI + "/" + client.id(),
      HttpMethod.DELETE,
      new HttpEntity<>(null,headers),
      new ParameterizedTypeReference<Map<String,Boolean>>(){}
    );

    Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldGiveNotFound_whenDeletClient_forInvalidId() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());

    final Long id = 400L;

    final ResponseEntity<ErrorResponse> result =  testRestTemplate.exchange(
      BASE_URI + "/" + id,
      HttpMethod.DELETE,
      new HttpEntity<>(null,headers),
      new ParameterizedTypeReference<ErrorResponse>(){}
    );

    Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldGiveForbidden_whenDeleteClient_forUnauthorizeUser() {
    final AuthResponse cred1 = adminCred();

    final HttpHeaders headers1 = new HttpHeaders();
    headers1.add("Authorization", "Bearer " + cred1.accessToken());
    
    final ClientRequest request = new ClientRequest("Bob", "bob@test.in", "+917878787878");
    final ClientResponse client =  testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(request,headers1),
      ClientResponse.class
    ).getBody();

    final AuthResponse cred2 = userCred();

    final HttpHeaders headers2 = new HttpHeaders();
    headers1.add("Authorization", "Bearer " + cred2.accessToken());

    final ResponseEntity<ErrorResponse> result =  testRestTemplate.exchange(
      BASE_URI + "/" + client.id(),
      HttpMethod.DELETE,
      new HttpEntity<>(null,headers2),
      new ParameterizedTypeReference<ErrorResponse>(){}
    );

    Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldGiveForbidden_whenDeletClient_forMissingAuthHeaders() {
    final AuthResponse cred = adminCred();

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + cred.accessToken());
    
    final ClientRequest request = new ClientRequest("Bob", "bob@test.in", "+917878787878");
    final ClientResponse client =  testRestTemplate.exchange(
      BASE_URI,
      HttpMethod.POST,
      new HttpEntity<>(request,headers),
      ClientResponse.class
    ).getBody();


    final ResponseEntity<ErrorResponse> result =  testRestTemplate.exchange(
      BASE_URI + "/" + client.id(),
      HttpMethod.DELETE,
      new HttpEntity<>(null),
      new ParameterizedTypeReference<ErrorResponse>(){}
    );

    Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
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
