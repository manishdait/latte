package com.example.latte_api.role;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.latte_api.role.authority.Authority;
import com.example.latte_api.role.authority.AuthorityRepository;
import com.example.latte_api.role.dto.RoleRequest;
import com.example.latte_api.role.dto.RoleResponse;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {
  private final AuthorityRepository authorityRepository;
  private final RoleRepository roleRepository;

  public List<RoleResponse> getRoles() {
    return roleRepository.findAll().stream()
    .map(r -> new RoleResponse(r.getId(), r.getRole(), r.getAuthorities().stream().map(a -> a.getAuthority()).toList()))
    .toList();
  }

  @Transactional
  public RoleResponse createRole(RoleRequest request) {
    roleRepository.findByRole(request.role()).ifPresent((r) -> {
      throw new IllegalArgumentException("Role already exist");
    });

    List<Authority> authorities = new ArrayList<>();

    for (String authority : request.authorities()) {
      authorities.add(authorityRepository.findByAuthority(authority).orElseThrow(
        () -> new EntityNotFoundException("Authority does not exist")
      ));
    }

    Role role = Role.builder()
      .role(request.role())
      .authorities(authorities)
      .build();
    roleRepository.save(role);

    return new RoleResponse(role.getId(), role.getRole(), role.getAuthorities().stream().map(a -> a.getAuthority()).toList());
  }
}
