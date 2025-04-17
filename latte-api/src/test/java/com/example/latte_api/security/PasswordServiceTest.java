package com.example.latte_api.security;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.latte_api.role.Role;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;
import com.example.latte_api.user.dto.ResetPasswordRequest;
import com.example.latte_api.user.dto.UserResponse;
import com.example.latte_api.user.mapper.UserMapper;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class PasswordServiceTest {
  private PasswordService passwordService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserMapper userMapper;

  @Captor
  ArgumentCaptor<User> userCaptor;

  @BeforeEach
  void setup() {
    passwordService = new PasswordService(userRepository, passwordEncoder, userMapper);
  }

  @AfterEach
  void purge() {
    passwordService = null;
  }

  @Test
  void shouldReturn_userDto_forPasswordUpdate_ofAuthenticatedUser() {
    // mock
    final User user = User.builder()
      .id(101L)
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .editable(true)
      .deletable(true)
      .role(Role.builder().role("USER").build())
      .build();
    final String encodedPass = "encoded-pass";
    final UserResponse userDto = Mockito.mock(UserResponse.class);

    // given
    final ResetPasswordRequest request = new ResetPasswordRequest("updated-pass", "updated-pass");
    final Authentication authentication = Mockito.mock(Authentication.class);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(passwordEncoder.encode(eq(request.updatePassword()))).thenReturn(encodedPass);
    when(userMapper.mapToUserDto(user)).thenReturn(userDto);

    final UserResponse result = passwordService.resetPassword(request, authentication);

    // then
    verify(authentication, times(1)).getPrincipal();
    verify(passwordEncoder, times(1)).encode(eq(request.updatePassword()));
    verify(userRepository, times(1)).save(userCaptor.capture());
    verify(userMapper, times(1)).mapToUserDto(user);

    final User updated = userCaptor.getValue();

    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(updated.getPassword()).isEqualTo(encodedPass);
  }

  @Test
  void shouldThrow_exception_forPasswordUpdate_ifUserisNotEditable() {
    // mock
    final User user = User.builder()
      .id(101L)
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .editable(false)
      .deletable(true)
      .role(Role.builder().role("USER").build())
      .build();

    // given
    final ResetPasswordRequest request = new ResetPasswordRequest("updated-pass", "updated-pass");
    final Authentication authentication = Mockito.mock(Authentication.class);

    // when
    when(authentication.getPrincipal()).thenReturn(user);

    Assertions.assertThatThrownBy(() -> passwordService.resetPassword(request, authentication))
      .isInstanceOf(IllegalStateException.class);

  }

  @Test
  void shouldThrow_exception_forPasswordUpdate_ifUpdatePassword_andConfirmPassword_diff() {
    // mock
    final User user = Mockito.mock(User.class);

    // given
    final ResetPasswordRequest request = new ResetPasswordRequest("updated-pass", "vonfirm-pass");
    final Authentication authentication = Mockito.mock(Authentication.class);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(user.isEditable()).thenReturn(true);

    // then
    Assertions.assertThatThrownBy(() ->  passwordService.resetPassword(request, authentication))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldReturn_userDto_forPasswordUpdate_forEmail() {
    // mock
    final User user = User.builder()
      .id(101L)
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .editable(true)
      .deletable(true)
      .role(Role.builder().role("USER").build())
      .build();
    final String encodedPass = "encoded-pass";
    final UserResponse userDto = Mockito.mock(UserResponse.class);

    // given
    final String email = "peter@test.in";
    final ResetPasswordRequest request = new ResetPasswordRequest("updated-pass", "updated-pass");

    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(passwordEncoder.encode(eq(request.updatePassword()))).thenReturn(encodedPass);
    when(userMapper.mapToUserDto(user)).thenReturn(userDto);

    final UserResponse result = passwordService.resetPassword(request, email);

    // then
    verify(userRepository, times(1)).findByEmail(email);
    verify(passwordEncoder, times(1)).encode(eq(request.updatePassword()));
    verify(userRepository, times(1)).save(userCaptor.capture());
    verify(userMapper, times(1)).mapToUserDto(user);

    final User updated = userCaptor.getValue();

    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(updated.getPassword()).isEqualTo(encodedPass);
  }

  @Test
  void shouldThrow_exception_forPasswordUpdate_forEmail_userIsNotEditable() {
    // mock
    final User user = User.builder()
      .id(101L)
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .editable(false)
      .deletable(true)
      .role(Role.builder().role("USER").build())
      .build();

    // given
    final String email = "peter@test.in";
    final ResetPasswordRequest request = new ResetPasswordRequest("updated-pass", "updated-pass");

    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    Assertions.assertThatThrownBy(() -> passwordService.resetPassword(request, email))
      .isInstanceOf(IllegalStateException.class);
  }


  @Test
  void shouldThrow_exception_forPasswordUpdate_forUser_ifUpdatePassword_adnConfirmPassword_diff() {
    // mock
    final User user = Mockito.mock(User.class);

    // given
    final ResetPasswordRequest request = new ResetPasswordRequest("updated-pass", "vonfirm-pass");
    final String email = "peter@gmail.com";

    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(user.isEditable()).thenReturn(true);

    // then
    Assertions.assertThatThrownBy(() ->  passwordService.resetPassword(request, email))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrow_exception_forPasswordUpdate_forInvalidUser() {
    // given
    final ResetPasswordRequest request = new ResetPasswordRequest("updated-pass", "vonfirm-pass");
    final String email = "peter@gmail.com";

    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // then
    Assertions.assertThatThrownBy(() ->  passwordService.resetPassword(request, email))
      .isInstanceOf(EntityNotFoundException.class);
  }
}
