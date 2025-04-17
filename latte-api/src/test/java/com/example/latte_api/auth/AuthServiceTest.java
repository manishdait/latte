package com.example.latte_api.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.latte_api.auth.dto.AuthRequest;
import com.example.latte_api.auth.dto.AuthResponse;
import com.example.latte_api.auth.dto.RegistrationRequest;
import com.example.latte_api.role.Role;
import com.example.latte_api.role.RoleRepository;
import com.example.latte_api.role.authority.Authority;
import com.example.latte_api.security.JwtProvider;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
  private AuthService authService;

  @Mock
  private UserRepository userRepository;
  
  @Mock
  private RoleRepository roleRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private JwtProvider jwtProvider;

  @Captor
  private ArgumentCaptor<User> useCaptor;

  @BeforeEach
  void setup() {
  authService = new AuthService(userRepository, roleRepository, passwordEncoder, authenticationManager, jwtProvider);
  }

  @AfterEach
  void purge() {
    authService = null;
  }

  @Test
  void shouldRegisterUser_forValidRequest() {
    // mock
    final String encodedPass = "encoded-pass";
    final Role role = Role.builder().id(101L).role("USER").build();

    // given
    final RegistrationRequest request = new RegistrationRequest("Peter", "peter@test.in", "Peter@01", "USER");

    // when
    when(userRepository.findByEmailOrFirstname(request.email(), request.firstname())).thenReturn(Optional.empty());
    when(roleRepository.findByRole(request.role())).thenReturn(Optional.of(role));
    when(passwordEncoder.encode(request.password())).thenReturn(encodedPass);

    authService.registerUser(request);

    // then
    verify(userRepository, times(1)).findByEmailOrFirstname(request.email(), request.firstname());
    verify(roleRepository, times(1)).findByRole(request.role());
    verify(userRepository, times(1)).save(useCaptor.capture());

    final User saved = useCaptor.getValue();

    Assertions.assertThat(saved.getFirstname()).isEqualTo(request.firstname());
    Assertions.assertThat(saved.getEmail()).isEqualTo(request.email());
    Assertions.assertThat(saved.getRole()).isEqualTo(role);
    Assertions.assertThat(saved.getPassword()).isEqualTo(encodedPass);
  }

  @Test
  void shouldThrow_exception_forDuplicateUser() {
    // mock
    final User user = Mockito.mock(User.class);

    // given
    final RegistrationRequest request = new RegistrationRequest("Peter", "peter@test.in", "Peter@01", "USER");

    // when
    when(userRepository.findByEmailOrFirstname(request.email(), request.firstname())).thenReturn(Optional.of(user));

    // then
    Assertions.assertThatThrownBy(() -> authService.registerUser(request))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrow_exception_forInvalidRole() {
    // given
    final RegistrationRequest request = new RegistrationRequest("Peter", "peter@test.in", "Peter@01", "USER");

    // when
    when(userRepository.findByEmailOrFirstname(request.email(), request.firstname())).thenReturn(Optional.empty());
    when(roleRepository.findByRole(request.role())).thenReturn(Optional.empty());

    // then
    Assertions.assertThatThrownBy(() -> authService.registerUser(request))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldReturn_authResponse_forValidCred() {
    // mock
    final Authentication authentication = Mockito.mock(Authentication.class);
    final Role role = Role.builder().role("User").authorities(List.of(Authority.builder().authority("ticket:create").build())).build();
    final User user = User.builder()
      .id(101L)
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .role(role)
      .build();
    final String accessToken = "acess-token";
    final String refreshToken = "refresh-token";

    // given
    final AuthRequest request = new AuthRequest("peter@test.in", "Peter@01");

    // when
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);
    when(jwtProvider.generateToken(eq(user.getEmail()), eq(Map.of())))
      .thenReturn(accessToken);
    when(jwtProvider.generateToken(eq(user.getEmail()), eq(Map.of()), eq(604800)))
      .thenReturn(refreshToken);

    final AuthResponse result = authService.authenticateUser(request);

    // then
    verify(authenticationManager, times(1))
      .authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(authentication, times(1)).getPrincipal();
    verify(jwtProvider, times(1))
      .generateToken(eq(user.getEmail()), eq(Map.of()));
    verify(jwtProvider, times(1))
      .generateToken(eq(user.getEmail()), eq(Map.of()), eq(604800));

    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result.email()).isEqualTo(request.email());
    Assertions.assertThat(result.role().role()).isEqualTo("User");
    Assertions.assertThat(result.accessToken()).isEqualTo(accessToken);
    Assertions.assertThat(result.refreshToken()).isEqualTo(refreshToken);
  }

  @Test
  void shouldThrow_exception_forInvalidCred() {
    // given
    final AuthRequest  request = new AuthRequest("peter@test.in", "Peter@01");

    // when
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
      .thenThrow(new BadCredentialsException("Invalid credentials"));
    
    // then
    Assertions.assertThatThrownBy(() -> authService.authenticateUser(request));
  }

  @Test
  void shouldReturn_authResponse_withRefeshAccessToken_forvalidToken() {
    // mock
    final Role role = Role.builder().role("User").authorities(List.of(Authority.builder().authority("create:ticket").build())).build();
    final User user = User.builder()
      .id(101L)
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .role(role)
      .build();
    final String username = "peter@test.in";
    final String refershToken = "refresh-token";
    final String accessToken = "access-token";
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

    // when
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refershToken);
    when(jwtProvider.getUsername(refershToken)).thenReturn(username);
    when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
    when(jwtProvider.validToken(user, refershToken)).thenReturn(true);
    when(jwtProvider.generateToken(eq(user.getEmail()), eq(Map.of())))
      .thenReturn(accessToken);

    final AuthResponse result = authService.refreshToken(request);
    
    // then
    verify(request, times(1)).getHeader(HttpHeaders.AUTHORIZATION);
    verify(jwtProvider, times(1)).getUsername(refershToken);
    verify(userRepository, times(1)).findByEmail(username);
    verify(jwtProvider, times(1)).validToken(user, refershToken);
    verify(jwtProvider, times(1)).generateToken(eq(user.getEmail()), eq(Map.of()));

    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result.email()).isEqualTo(username);
    Assertions.assertThat(result.accessToken()).isEqualTo(accessToken);
    Assertions.assertThat(result.refreshToken()).isEqualTo(refershToken);
  }

  @Test
  void shouldThrow_exception_forRefreshToken_ifHeaderIsEmpty() {
    // mock
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

    // when
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
    
    //then
    Assertions.assertThatThrownBy(() -> authService.refreshToken(request));
  }

  @Test
  void shouldThrow_exception_forRefreshToken_ifHeaderIsInvalid() {
    // mock
    final String authorizationHeader = "token";
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

    // when
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authorizationHeader);
    
    //then
    Assertions.assertThatThrownBy(() -> authService.refreshToken(request));
  }

  @Test
  void shouldThrow_exception_forRefreshToken_ifUserIsInvalid() {
    // mock
    final String refreshToken = "refresh-token";
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

    // when
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
    when(jwtProvider.getUsername(refreshToken)).thenReturn(null);
    
    //then
    Assertions.assertThatThrownBy(() -> authService.refreshToken(request));
  }

  @Test
  void shouldThrow_exception_forRefreshToken_ifUserNotPresentInDatabase() {
    // mock
    final String refreshToken = "refresh-token";
    final String username = "louis@gmail.com";
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

    // when
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
    when(jwtProvider.getUsername(refreshToken)).thenReturn(username);
    when(userRepository.findByEmail(username)).thenReturn(Optional.empty());
    
    //then
    Assertions.assertThatThrownBy(() -> authService.refreshToken(request));
  }

  @Test
  void shouldThrow_exception_forRefreshToken_ifTokenInvalid() {
    // mock
    final String refreshToken = "refresh-token";
    final String username = "louis@gmail.com";
    final User user = Mockito.mock(User.class);
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

    // when
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
    when(jwtProvider.getUsername(refreshToken)).thenReturn(username);
    when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
    when(jwtProvider.validToken(user, refreshToken)).thenReturn(false);
    
    //then
    Assertions.assertThatThrownBy(() -> authService.refreshToken(request));
  }
}
