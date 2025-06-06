package com.example.latte_api.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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

import com.example.latte_api.activity.ActivityRepository;
import com.example.latte_api.role.Role;
import com.example.latte_api.role.RoleRepository;
import com.example.latte_api.role.authority.Authority;
import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.ticket.TicketRepository;
import com.example.latte_api.user.dto.UserRequest;
import com.example.latte_api.user.dto.UserResponse;
import com.example.latte_api.user.mapper.UserMapper;

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

  @Mock
  private TicketRepository ticketRepository;

  @Mock
  private ActivityRepository activityRepository;

  @Captor
  private ArgumentCaptor<User> userCaptor;

  @BeforeEach
  void setup() {
    userService = new UserService(userRepository, roleRepository, ticketRepository, activityRepository, userMapper);
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
      .isInstanceOf(UsernameNotFoundException.class);
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

    final PagedEntity<UserResponse> result = userService.getUsers(page, size);

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
    final UserResponse userDto = Mockito.mock(UserResponse.class);
    
    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(userMapper.mapToUserDto(user)).thenReturn(userDto);

    final UserResponse result = userService.getUser(authentication);

    // then
    verify(authentication, times(1)).getPrincipal();
    verify(userMapper, times(1)).mapToUserDto(user);

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  void shouldReturn_userDto_forEmail() {
    // mock
    final User user = Mockito.mock(User.class);
    final UserResponse userDto = Mockito.mock(UserResponse.class);

    // given
    final String email = "peter@test.in";
    
    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(userMapper.mapToUserDto(user)).thenReturn(userDto);

    final UserResponse result = userService.getUser(email);

    // then
    verify(userRepository, times(1)).findByEmail(email);
    verify(userMapper, times(1)).mapToUserDto(user);

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  void shouldThrow_exception_whenGetUserByEmail_forInvalidEmail() {
    // given
    final String email = "invalid@test.in";
    
    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    Assertions.assertThatThrownBy(() -> userService.getUser(email))
      .isInstanceOf(EntityNotFoundException.class);
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
      .role(Role.builder().role("User").authorities(List.of(Authority.builder().authority("ticket::create").build())).build())
      .build();
    final UserResponse userDto = Mockito.mock(UserResponse.class);

    // given
    final UserRequest request = new UserRequest("Stewie", "stewie@test.in", null);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(userMapper.mapToUserDto(user)).thenReturn(userDto);

    final UserResponse result = userService.updateUser(request, authentication);

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
      .role(Role.builder().role("User").build())
      .build();
    final UserResponse userDto = Mockito.mock(UserResponse.class);

    // given
    final String email = "peter@test.in";
    final UserRequest request = new UserRequest("Stewie", "stewie@test.in", "User");

    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(userMapper.mapToUserDto(user)).thenReturn(userDto);

    final UserResponse result = userService.updateUser(request, email);

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
    final UserRequest request = new UserRequest("Stewie", "stewie@test.in", "USER");

    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // then
    Assertions.assertThatThrownBy(() -> userService.updateUser(request, email))
      .isInstanceOf(EntityNotFoundException.class);
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
    final UserRequest request = new UserRequest("Stewie", "stewie@test.in", "User");

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
    final UserResponse userDto = Mockito.mock(UserResponse.class);
    final Role role = Role.builder().id(101L).role("ADMIN").build();

    // given
    final String email = "peter@test.in";
    final UserRequest request = new UserRequest("Stewie", "stewie@test.in", "ADMIN");

    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(roleRepository.findByRole(request.role())).thenReturn(Optional.of(role));
    when(userMapper.mapToUserDto(user)).thenReturn(userDto);

    final UserResponse result = userService.updateUser(request, email);

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
    final UserRequest request = new UserRequest("Stewie", "stewie@test.in", "TEST");

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
    final User admin = Mockito.mock(User.class);

    // given
    final String email = "peter@test.in";

    // when
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(user.isDeletable()).thenReturn(true);
    when(userRepository.findByDeletable(false)).thenReturn(List.of(admin));
    when(ticketRepository.findByAssignedTo(user)).thenReturn(List.of());
    when(ticketRepository.findByCreatedBy(user)).thenReturn(List.of());
    when(activityRepository.findByAuthor(user)).thenReturn(List.of());

    userService.deleteUser(email);

    // then
    verify(userRepository, times(1)).findByEmail(email);
    verify(userRepository, times(1)).findByDeletable(false);
    verify(ticketRepository, times(1)).findByAssignedTo(user);
    verify(ticketRepository, times(1)).findByCreatedBy(user);
    verify(activityRepository, times(1)).findByAuthor(user);
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
}
