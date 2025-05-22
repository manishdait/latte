package com.example.latte_api.role;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.example.latte_api.role.authority.Authority;
import com.example.latte_api.role.authority.AuthorityRepository;
import com.example.latte_api.role.dto.RoleRequest;
import com.example.latte_api.role.dto.RoleResponse;
import com.example.latte_api.role.mapper.RoleMapper;
import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {
  private RoleService roleService;

  @Mock
  private AuthorityRepository authorityRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private RoleMapper roleMapper;

  @Mock
  private UserRepository userRepository;

  @Captor
  private ArgumentCaptor<Role> roleCaptor;

  @BeforeEach
  void setup() {
    roleService = new RoleService(authorityRepository, userRepository, roleRepository, roleMapper);
  }

  @AfterEach
  void purge() {
    roleService = null;
  }

  @Test
  void shouldReturn_paged_roleResponseList() {
    // mock
    @SuppressWarnings("unchecked")
    final Page<Role> page = Mockito.mock(Page.class);

    final Role role = new Role(101L, "User", true, true, List.of(), List.of());
    Pageable pageable = PageRequest.of(0,1, Sort.by(Direction.ASC, "id")); 
    // when
    when(roleRepository.findAll(pageable)).thenReturn(page);
    when(page.hasNext()).thenReturn(false);
    when(page.hasPrevious()).thenReturn(false);
    when(page.getContent()).thenReturn(List.of(role));

    final PagedEntity<RoleResponse> result = roleService.getRoles(0,1);

    // then
    verify(roleRepository, times(1)).findAll(pageable);
    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result.getContent()).hasSize(1);
  }

  @Test
  void shouldReturn_roleResponse_whenRoleCreated() {
    final RoleResponse roleResponse = Mockito.mock(RoleResponse.class);
    final Authority authority = new Authority(101L, "dev::auth", List.of());

    // given
    final RoleRequest request = new RoleRequest("Dev", List.of("dev::auth"));

    // when
    when(roleRepository.findByRole(request.role())).thenReturn(Optional.empty());
    when(authorityRepository.findByAuthority(eq("dev::auth"))).thenReturn(Optional.of(authority));
    when(roleMapper.mapToRoleResponse(any(Role.class))).thenReturn(roleResponse);
    final RoleResponse result = roleService.createRole(request);

    // then
    verify(roleRepository, times(1)).findByRole(request.role());
    verify(authorityRepository, times(1)).findByAuthority(eq("dev::auth"));
    verify(roleRepository, times(1)).save(roleCaptor.capture());
    verify(roleMapper, times(1)).mapToRoleResponse(any(Role.class));

    Assertions.assertThat(result).isNotNull();

    final Role capture = roleCaptor.getValue();
    Assertions.assertThat(capture.getRole()).isEqualTo(request.role());
    Assertions.assertThat(capture.getAuthorities()).isEqualTo(List.of(new SimpleGrantedAuthority("dev::auth")));
  }

  @Test
  void shouldThrow_exception_whenRoleCreated_forInvalidAuthority() {
    // given
    final RoleRequest request = new RoleRequest("Dev", List.of("dev::auth"));

    // when
    when(roleRepository.findByRole(request.role())).thenReturn(Optional.empty());
    when(authorityRepository.findByAuthority(eq("dev::auth"))).thenReturn(Optional.empty());
    
    Assertions.assertThatThrownBy(() -> roleService.createRole(request))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldThrow_exception_whenRoleCreated_forDuplicateRole() {
    // mock
    Role role = Mockito.mock(Role.class);

    // given
    final RoleRequest request = new RoleRequest("Dev", List.of("dev::auth"));

    // when
    when(roleRepository.findByRole(request.role())).thenReturn(Optional.of(role));
    
    Assertions.assertThatThrownBy(() -> roleService.createRole(request))
      .isInstanceOf(IllegalArgumentException.class);
  }
  
  @Test
  void shouldUpdate_roleNameAndReturn_roleResponse() {
    final RoleResponse roleResponse = Mockito.mock(RoleResponse.class);
    final Role role = new Role(101L, "User", true, true, List.of(), List.of());

    final RoleRequest request = new RoleRequest("Dev", null);
    final long id = 101L;

    when(roleRepository.findById(id)).thenReturn(Optional.of(role));
    when(roleMapper.mapToRoleResponse(any(Role.class))).thenReturn(roleResponse);
    final RoleResponse result = roleService.editResponse(id, request);

    verify(roleRepository, times(1)).findById(id);
    verify(roleRepository, times(1)).save(roleCaptor.capture());
    verify(roleMapper, times(1)).mapToRoleResponse(any(Role.class));

    Assertions.assertThat(result).isNotNull();

    final Role capture = roleCaptor.getValue();
    Assertions.assertThat(capture.getRole()).isEqualTo("Dev");
  }

  @Test
  void shouldUpdate_roleAuthorityAndReturn_roleResponse() {
    final RoleResponse roleResponse = Mockito.mock(RoleResponse.class);
    final Authority authority = Mockito.mock(Authority.class);

    final Role role = new Role(101L, "User", true, true, List.of(), List.of());

    final RoleRequest request = new RoleRequest(null, List.of("dev::per"));
    final long id = 101L;

    when(roleRepository.findById(id)).thenReturn(Optional.of(role));
    when(authorityRepository.findByAuthority(eq("dev::per"))).thenReturn(Optional.of(authority));
    when(roleMapper.mapToRoleResponse(any(Role.class))).thenReturn(roleResponse);
    final RoleResponse result = roleService.editResponse(id, request);

    verify(roleRepository, times(1)).findById(id);
    verify(authorityRepository, times(1)).findByAuthority(eq("dev::per"));
    verify(roleRepository, times(1)).save(roleCaptor.capture());
    verify(roleMapper, times(1)).mapToRoleResponse(any(Role.class));

    Assertions.assertThat(result).isNotNull();

    final Role capture = roleCaptor.getValue();
    Assertions.assertThat(capture.getRole()).isEqualTo("User");
  }

  @Test
  void shouldThrow_exceptionIfUserNotEditable_onUpdate() {
    final Role role = new Role(101L, "User", false, true, List.of(), List.of());

    final RoleRequest request = new RoleRequest("Dev", null);
    final long id = 101L;

    when(roleRepository.findById(id)).thenReturn(Optional.of(role));
    Assertions.assertThatThrownBy(() -> roleService.editResponse(id, request))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldThrow_exceptionIfInvalidId_onUpdate() {
    final RoleRequest request = new RoleRequest("Dev", null);
    final long id = 102L;

    when(roleRepository.findById(id)).thenReturn(Optional.empty());
    Assertions.assertThatThrownBy(() -> roleService.editResponse(id, request))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldDelete_roleAndUpdateUserToNewRole() {
    final User user1 = Mockito.mock(User.class);
    final User user2 = Mockito.mock(User.class);

    final Role role1 = new Role(101L, "User", true, true, List.of(), List.of());
    final Role role2 = new Role(101L, "User", true, true, List.of(), List.of());

    final long id = 101;
    final long newId = 102;

    when(roleRepository.findById(id)).thenReturn(Optional.of(role1));
    when(roleRepository.findById(newId)).thenReturn(Optional.of(role2));
    when(userRepository.findByRole(role1)).thenReturn(List.of(user1, user2));
    roleService.deleteRole(id, newId);

    verify(roleRepository, times(1)).findById(id);
    verify(roleRepository, times(1)).findById(newId);
    verify(userRepository, times(1)).saveAll(anyList());
  }

  @Test
  void shouldThrow_exceptionIfRoleNotDeletable_onDeleteRole() {
    final Role role1 = new Role(101L, "User", true, false, List.of(), List.of());

    final long id = 101;
    final long newId = 102;

    when(roleRepository.findById(id)).thenReturn(Optional.of(role1));
    Assertions.assertThatThrownBy(() -> roleService.deleteRole(id, newId))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldThrow_exceptionIfRoleNotPresent_onDeleteRole() {
    final long id = 101;
    final long newId = 102;

    when(roleRepository.findById(id)).thenReturn(Optional.empty());
    Assertions.assertThatThrownBy(() -> roleService.deleteRole(id, newId))
      .isInstanceOf(EntityNotFoundException.class);
  }
}
