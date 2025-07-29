package com.example.latte_api.role;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.example.latte_api.TestUtils.TEST_AUTHORITY_WRITE;
import static com.example.latte_api.TestUtils.TEST_AUTHORITY_READ;
import static com.example.latte_api.TestUtils.createAuthority;
import static com.example.latte_api.TestUtils.createAdminRole;
import static com.example.latte_api.TestUtils.createUserRole;

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

/*
 * Role Service Test
 */
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

    final Role role = createUserRole();
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
    final Authority authority = createAuthority(101L, TEST_AUTHORITY_READ);

    // given
    final RoleRequest request = new RoleRequest("Test Role", List.of(TEST_AUTHORITY_READ));

    // when
    when(roleRepository.findByRole(request.role())).thenReturn(Optional.empty());
    when(authorityRepository.findByAuthorityIgnoreCase(eq(TEST_AUTHORITY_READ)))
      .thenReturn(Optional.of(authority));
    when(roleMapper.mapToRoleResponse(any(Role.class))).thenReturn(roleResponse);
    final RoleResponse result = roleService.createRole(request);

    // then
    verify(roleRepository, times(1)).findByRole(request.role());
    verify(authorityRepository, times(1)).findByAuthorityIgnoreCase(eq(TEST_AUTHORITY_READ));
    verify(roleRepository, times(1)).save(roleCaptor.capture());
    verify(roleMapper, times(1)).mapToRoleResponse(any(Role.class));

    Assertions.assertThat(result).isNotNull();

    final Role capture = roleCaptor.getValue();
    Assertions.assertThat(capture.getRole()).isEqualTo(request.role());
    Assertions.assertThat(capture.getAuthorities()).isEqualTo(List.of(new SimpleGrantedAuthority(TEST_AUTHORITY_READ)));
  }

  @Test
  void shouldThrow_exception_whenRoleCreated_forInvalidAuthority() {
    // given
    final RoleRequest request = new RoleRequest("Test Role", List.of("unknow::authority"));

    // when
    when(roleRepository.findByRole(request.role())).thenReturn(Optional.empty());
    when(authorityRepository.findByAuthorityIgnoreCase(eq("unknow::authority"))).thenReturn(Optional.empty());
    
    Assertions.assertThatThrownBy(() -> roleService.createRole(request))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldThrow_exception_whenRoleCreated_forDuplicateRole() {
    // mock
    Role role = Mockito.mock(Role.class);

    // given
    final RoleRequest request = new RoleRequest("Test Role", List.of(TEST_AUTHORITY_READ));

    // when
    when(roleRepository.findByRole(request.role())).thenReturn(Optional.of(role));
    
    Assertions.assertThatThrownBy(() -> roleService.createRole(request))
      .isInstanceOf(IllegalArgumentException.class);
  }
  
  @Test
  void shouldUpdate_roleNameAndReturn_roleResponse() {
    final RoleResponse roleResponse = Mockito.mock(RoleResponse.class);
    final Role role = createUserRole();
    final String updatedRoleName = "Updated Role";

    final RoleRequest request = new RoleRequest(updatedRoleName, null);
    final long id = 102L;

    when(roleRepository.findById(id)).thenReturn(Optional.of(role));
    when(roleRepository.findByRole(updatedRoleName)).thenReturn(Optional.empty());
    when(roleMapper.mapToRoleResponse(any(Role.class))).thenReturn(roleResponse);
    final RoleResponse result = roleService.updateRole(id, request);

    verify(roleRepository, times(1)).findById(id);
    verify(roleRepository, times(1)).findByRole(updatedRoleName);
    verify(roleRepository, times(1)).save(roleCaptor.capture());
    verify(roleMapper, times(1)).mapToRoleResponse(any(Role.class));

    Assertions.assertThat(result).isNotNull();

    final Role capture = roleCaptor.getValue();
    Assertions.assertThat(capture.getRole()).isEqualTo(updatedRoleName);
  }

  @Test
  void shouldThrow_exceptionIfUpdateRoleName_alreadyExist_onUpdatingRole() {
    final Role mockRole = Mockito.mock(Role.class);
    final Role role = createUserRole();
    
    final String updatedRoleName = "Updated Role";
    final RoleRequest request = new RoleRequest(updatedRoleName, null);
    final long id = 102L;

    when(roleRepository.findById(id)).thenReturn(Optional.of(role));
    when(roleRepository.findByRole(updatedRoleName)).thenReturn(Optional.of(mockRole));
    Assertions.assertThatThrownBy(() -> roleService.updateRole(id, request))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldUpdate_roleAuthorityAndReturn_roleResponse() {
    final RoleResponse roleResponse = Mockito.mock(RoleResponse.class);
    final String updatedAuthorityName = TEST_AUTHORITY_WRITE;
    final Authority authority = createAuthority(102L, TEST_AUTHORITY_WRITE);
    final Role role = createUserRole();

    final RoleRequest request = new RoleRequest(null, List.of(updatedAuthorityName));
    final long id = 102L;

    when(roleRepository.findById(id)).thenReturn(Optional.of(role));
    when(authorityRepository.findByAuthorityIgnoreCase(eq(updatedAuthorityName))).thenReturn(Optional.of(authority));
    when(roleMapper.mapToRoleResponse(any(Role.class))).thenReturn(roleResponse);
    final RoleResponse result = roleService.updateRole(id, request);

    verify(roleRepository, times(1)).findById(id);
    verify(authorityRepository, times(1)).findByAuthorityIgnoreCase(eq(updatedAuthorityName));
    verify(roleRepository, times(1)).save(roleCaptor.capture());
    verify(roleMapper, times(1)).mapToRoleResponse(any(Role.class));

    Assertions.assertThat(result).isNotNull();

    final Role capture = roleCaptor.getValue();
    Assertions.assertThat(capture.getRole()).isEqualTo("User");
    Assertions.assertThat(capture.getAuthorities()).hasSize(1)
      .contains(new SimpleGrantedAuthority(updatedAuthorityName));
  }

  @Test
  void shouldThrow_exceptionIfUserNotEditable_onUpdate() {
    final Role role = createAdminRole();

    final RoleRequest request = new RoleRequest("Update Admin", null);
    final long id = 101L;

    when(roleRepository.findById(id)).thenReturn(Optional.of(role));
    Assertions.assertThatThrownBy(() -> roleService.updateRole(id, request))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldThrow_exceptionIfInvalidId_onUpdate() {
    final RoleRequest request = new RoleRequest("Update Role", null);
    final long id = 103L;

    when(roleRepository.findById(id)).thenReturn(Optional.empty());
    Assertions.assertThatThrownBy(() -> roleService.updateRole(id, request))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldDelete_roleAndAssignNewRoleToUser() {
    final User user1 = Mockito.mock(User.class);
    final User user2 = Mockito.mock(User.class);

    final Role role1 = createUserRole();
    final Role role2 = createAdminRole();

    final long roleToDeleteId = 102L;
    final long roleToUpdateId = 101L;

    when(roleRepository.findById(roleToDeleteId)).thenReturn(Optional.of(role1));
    when(roleRepository.findById(roleToUpdateId)).thenReturn(Optional.of(role2));
    when(userRepository.findByRole(role1)).thenReturn(List.of(user1, user2));
    roleService.deleteRole(roleToDeleteId, roleToUpdateId);

    verify(roleRepository, times(1)).findById(roleToDeleteId);
    verify(roleRepository, times(1)).findById(roleToUpdateId);
    verify(userRepository, times(1)).saveAll(anyList());
  }

  @Test
  void shouldThrow_exceptionIfDeleteRoleAndUpdateRole_areSame_onDeleteRole() {
    final long roleToDeleteId = 101L;
    final long roleToUpdateId = 101L;
    Assertions.assertThatThrownBy(() -> roleService.deleteRole(roleToDeleteId, roleToUpdateId))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrow_exceptionIfRoleNotDeletable_onDeleteRole() {
    final Role role = createAdminRole();

    final long roleToDeleteId = 101L;
    final long roleToUpdateId = 102L;

    when(roleRepository.findById(roleToDeleteId)).thenReturn(Optional.of(role));
    Assertions.assertThatThrownBy(() -> roleService.deleteRole(roleToDeleteId, roleToUpdateId))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldThrow_exceptionIfRoleNotPresent_onDeleteRole() {
    final long roleToDeleteId = 103L;
    final long roleToUpdateId = 102L;

    when(roleRepository.findById(roleToDeleteId)).thenReturn(Optional.empty());
    Assertions.assertThatThrownBy(() -> roleService.deleteRole(roleToDeleteId, roleToUpdateId))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldThrow_exceptionIfRoleToUpdateIsNotPresent_onDeleteRole() {
    final Role role = createUserRole();

    final long roleToDeleteId = 101L;
    final long roleToUpdateId = 103L;

    when(roleRepository.findById(roleToDeleteId)).thenReturn(Optional.of(role));
    when(roleRepository.findById(roleToUpdateId)).thenReturn(Optional.empty());
    Assertions.assertThatThrownBy(() -> roleService.deleteRole(roleToDeleteId, roleToUpdateId))
      .isInstanceOf(EntityNotFoundException.class);
  }
}
