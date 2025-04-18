package com.example.latte_api.role;

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
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.example.latte_api.role.authority.Authority;
import com.example.latte_api.role.authority.AuthorityRepository;
import com.example.latte_api.role.dto.RoleRequest;
import com.example.latte_api.role.dto.RoleResponse;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {
  private RoleService roleService;

  @Mock
  private AuthorityRepository authorityRepository;

  @Mock
  private RoleRepository roleRepository;

  @Captor
  private ArgumentCaptor<Role> roleCaptor;

  @BeforeEach
  void setup() {
    roleService = new RoleService(authorityRepository, roleRepository);
  }

  @AfterEach
  void purge() {
    roleService = null;
  }

  @Test
  void shouldReturn_roleResponseList() {
    // mock
    final Role role = new Role(101L, "User", true, true, List.of(), List.of());

    // when
    when(roleRepository.findAll()).thenReturn(List.of(role));
    final List<RoleResponse> result = roleService.getRoles();

    // then
    verify(roleRepository, times(1)).findAll();
    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result).hasSize(1);
  }

  @Test
  void shouldReturn_roleResponse_whenRoleCreated() {
    final Authority authority = new Authority(101L, "dev::auth", List.of());

    // given
    final RoleRequest request = new RoleRequest("Dev", List.of("dev::auth"));

    // when
    when(roleRepository.findByRole(request.role())).thenReturn(Optional.empty());
    when(authorityRepository.findByAuthority(eq("dev::auth"))).thenReturn(Optional.of(authority));
    final RoleResponse result = roleService.createRole(request);

    // then
    verify(roleRepository, times(1)).findByRole(request.role());
    verify(authorityRepository, times(1)).findByAuthority(eq("dev::auth"));
    verify(roleRepository, times(1)).save(roleCaptor.capture());

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
}
