package com.example.latte_api.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.user.dto.UserDto;
import com.example.latte_api.user.mapper.UserMapper;
import com.example.latte_api.user.role.Role;
import com.example.latte_api.user.role.RoleRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
  private UserService userService;

  @Mock
  private UserRepository userRepository;
  @Mock
  private RoleRepository roleRepository;
  @Mock
  private UserMapper userMapper;

  @Captor
  private ArgumentCaptor<User> userCaptor;

  @BeforeEach
  void setup() {
    userService = new UserService(userRepository, roleRepository, userMapper);
  }

  @AfterEach
  void purge() {
    userService = null;
  }

  @Test
  void shouldReturn_userdetails_froValidUserEmail() {
    // mock
    final User user = Mockito.mock(User.class);

    // given
    final String email = "peter@test.in";

    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    final UserDetails result = userService.loadUserByUsername(email);

    // then
    verify(userRepository, times(1)).findByEmail(email);

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  void shouldThrow_exception_forInvalidUserEmail() {
    // given
    final String email = "louis@gmail.com";

    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // then
    Assertions.assertThatThrownBy(() -> userService.loadUserByUsername(email))
      .isInstanceOf(UsernameNotFoundException.class)
      .hasMessage("User with username:`louis@gmail.com` not found");
  }

  @Test
  void shouldReturn_pagedEntity_ofUserDto() {
    // mock
    @SuppressWarnings("unchecked")
    final Page<User> userPage = Mockito.mock(Page.class);

    // given
    final int page = 0;
    final int size = 10;

    // when
    when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

    final PagedEntity<UserDto> result = userService.getUsers(page, size);

    // then
    verify(userRepository, times(1)).findAll(any(Pageable.class));
    verify(userPage, times(1)).hasNext();
    verify(userPage, times(1)).hasPrevious();
    verify(userPage, times(1)).getContent();

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  void shouldReturn_pagedEntity_ofString() {
    // mock
    @SuppressWarnings("unchecked")
    final Page<User> userPage = Mockito.mock(Page.class);

    // given
    final int page = 0;
    final int size = 10;

    // when
    when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

    final PagedEntity<String> result = userService.getUserList(page, size);

    // then
    verify(userRepository, times(1)).findAll(any(Pageable.class));
    verify(userPage, times(1)).hasNext();
    verify(userPage, times(1)).hasPrevious();
    verify(userPage, times(1)).getContent();

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  void shouldReturn_authenticatedUserDto() {
    // mock
    final Authentication authentication = Mockito.mock(Authentication.class);
    final User user = Mockito.mock(User.class);
    final UserDto userDto = Mockito.mock(UserDto.class);
    
    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(userMapper.mapToUserDto(user)).thenReturn(userDto);

    final UserDto result = userService.getUser(authentication);

    // then
    verify(authentication, times(1)).getPrincipal();
    verify(userMapper, times(1)).mapToUserDto(user);

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  void shouldReturn_userDto_forEmail() {
    // mock
    final User user = Mockito.mock(User.class);
    final UserDto userDto = Mockito.mock(UserDto.class);

    // given
    final String email = "peter@test.in";
    
    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(userMapper.mapToUserDto(user)).thenReturn(userDto);

    final UserDto result = userService.getUser(email);

    // then
    verify(userRepository, times(1)).findByEmail(email);
    verify(userMapper, times(1)).mapToUserDto(user);

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  void shouldReturn_updatedUserDto_forAuhtenticatedUser() {
    // mock
    final Authentication authentication = Mockito.mock(Authentication.class);
    final User user = User.builder()
      .id(101L)
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .editable(true)
      .deletable(true)
      .role(Role.builder().role("USER").build())
      .build();
    final UserDto userDto = Mockito.mock(UserDto.class);

    // given
    final UserDto request = new UserDto("Stewie", "stewie@test.in", null, false, false);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(userMapper.mapToUserDto(user)).thenReturn(userDto);

    final UserDto result = userService.updateUser(request, authentication);

    // then
    verify(authentication, times(1)).getPrincipal();
    verify(userRepository, times(1)).save(userCaptor.capture());
    verify(userMapper, times(1)).mapToUserDto(user);

    final User update = userCaptor.getValue();

    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(update.getFirstname()).isEqualTo(request.firstname());
    Assertions.assertThat(update.getEmail()).isEqualTo(request.email());
  }

  @Test
  void shouldReturn_updatedUserDto_forGivenEmail() {
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
    final UserDto userDto = Mockito.mock(UserDto.class);

    // given
    final String email = "peter@test.in";
    final UserDto request = new UserDto("Stewie", "stewie@test.in", "USER", false, false);

    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(userMapper.mapToUserDto(user)).thenReturn(userDto);

    final UserDto result = userService.updateUser(request, email);

    // then
    verify(userRepository, times(1)).findByEmail(email);
    verify(userRepository, times(1)).save(userCaptor.capture());
    verify(userMapper, times(1)).mapToUserDto(user);

    final User update = userCaptor.getValue();

    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(update.getFirstname()).isEqualTo(request.firstname());
    Assertions.assertThat(update.getEmail()).isEqualTo(request.email());
  }

  @Test
  void shouldThrow_exceptionOnUpdate_forInvalidEmail() {
    // given
    final String email = "louis@gmail.com";
    final UserDto request = new UserDto("Stewie", "stewie@test.in", "USER", false, false);

    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // then
    Assertions.assertThatThrownBy(() -> userService.updateUser(request, email));
  }

  @Test
  void shouldThrow_exceptionOnUpdate_forUserNotEditable() {
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
    final String email = "peter@test.com";
    final UserDto request = new UserDto("Stewie", "stewie@test.in", "USER", false, false);

    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    // then
    Assertions.assertThatThrownBy(() -> userService.updateUser(request, email))
      .isInstanceOf(IllegalStateException.class);
  }


  @Test
  void shouldReturn_updatedUserDto_withRole_forGivenEmail() {
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
    final UserDto userDto = Mockito.mock(UserDto.class);
    final Role role = Role.builder().id(101L).role("ADMIN").build();

    // given
    final String email = "peter@test.in";
    final UserDto request = new UserDto("Stewie", "stewie@test.in", "ADMIN", false, false);

    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(roleRepository.findByRole(request.role())).thenReturn(Optional.of(role));
    when(userMapper.mapToUserDto(user)).thenReturn(userDto);

    final UserDto result = userService.updateUser(request, email);

    // then
    verify(userRepository, times(1)).findByEmail(email);
    verify(userRepository, times(1)).save(userCaptor.capture());
    verify(roleRepository, times(1)).findByRole(request.role());
    verify(userMapper, times(1)).mapToUserDto(user);

    final User update = userCaptor.getValue();

    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(update.getFirstname()).isEqualTo(request.firstname());
    Assertions.assertThat(update.getEmail()).isEqualTo(request.email());
    Assertions.assertThat(update.getRole()).isEqualTo(role);
  }

  @Test
  void shouldThrow_exceptionOnUpdate_ifRoleNotPresent() {
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

    // given
    final String email = "peter@test.in";
    final UserDto request = new UserDto("Stewie", "stewie@test.in", "TEST", false, false);

    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(roleRepository.findByRole(request.role())).thenReturn(Optional.empty());

    // then
    Assertions.assertThatThrownBy(() -> userService.updateUser(request, email))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldDelete_user_forValidEmail() {
    // mock
    final User user = Mockito.mock(User.class);

    // given
    final String email = "peter@test.in";

    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(user.isDeletable()).thenReturn(true);
    userService.deleteUser(email);

    // then
    verify(userRepository, times(1)).findByEmail(email);
    verify(userRepository, times(1)).delete(user);
  }

   @Test
  void shouldthrow_exception_onDelete_forNotDeletableUser() {
    // mock
    final User user = Mockito.mock(User.class);

    // given
    final String email = "peter@test.in";

    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(user.isDeletable()).thenReturn(false);
    // then
    Assertions.assertThatThrownBy(() -> userService.deleteUser(email))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldthrow_exception_onDelete_forInvalidEmail() {
    // given
    final String email = "louis@test.in";

    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // then
    Assertions.assertThatThrownBy(() -> userService.deleteUser(email))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldReturn_countOfUsers() {
    when(userRepository.count()).thenReturn(1L);
    final Map<String, Long> result = userService.getUserCount();
    verify(userRepository, times(1)).count();

    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result).isEqualTo(Map.of("user_count", 1L));
  }
}
