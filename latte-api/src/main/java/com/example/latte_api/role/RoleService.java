package com.example.latte_api.role;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.latte_api.role.authority.Authority;
import com.example.latte_api.role.authority.AuthorityRepository;
import com.example.latte_api.role.dto.RoleRequest;
import com.example.latte_api.role.dto.RoleResponse;
import com.example.latte_api.role.mapper.RoleMapper;
import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {
  private final AuthorityRepository authorityRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  private final RoleMapper roleMapper;

  public PagedEntity<RoleResponse> getRoles(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Role> rolePage =  roleRepository.findAll(pageable);

    PagedEntity<RoleResponse> respone = new PagedEntity<>();
    respone.setNext(rolePage.hasNext());
    respone.setPrev(rolePage.hasPrevious());
    respone.setContent(rolePage.getContent().stream().map(r -> roleMapper.mapToRoleResponse(r)).toList());

    return respone;
  }

  public RoleResponse getRole(Long id) {
    Role role = roleRepository.findById(id).orElseThrow(
      () -> new EntityNotFoundException("Role not found")
    );
    return roleMapper.mapToRoleResponse(role);
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
      .editable(true)
      .deletable(true)
      .build();
    roleRepository.save(role);

    return roleMapper.mapToRoleResponse(role);
  }

  @Transactional
  public RoleResponse editResponse(Long id, RoleRequest request) {
    Role role = roleRepository.findById(id).orElseThrow(
      () -> new EntityNotFoundException("Role not found")
    );

    if (!role.isEditable()) {
      throw new IllegalStateException("Role can not be edit");
    }

    if (request.role() != null && !request.role().equals(role.getRole())) {
      role.setRole(request.role());
    }

    if (request.authorities() != null) {
      List<Authority> authorities = new ArrayList<>();

      for (String authority : request.authorities()) {
        authorities.add(authorityRepository.findByAuthority(authority).orElseThrow(
          () -> new EntityNotFoundException("Authority does not exist")
        ));
      }

      role.setAuthorities(authorities);
    }

    roleRepository.save(role);
    return roleMapper.mapToRoleResponse(role);
  }

  @Transactional
  public void deleteRole(Long id, Long newId) {
    if (id == newId) {
      throw new IllegalArgumentException("Role delete id and Update id can not be same");
    }

    Role prevRole = roleRepository.findById(id).orElseThrow(
      () -> new EntityNotFoundException("Role not found")
    );

    if(!prevRole.isDeletable()) {
      throw new IllegalStateException("Role can not be delete");
    }

    Role newRole = roleRepository.findById(newId).orElseThrow(
      () -> new EntityNotFoundException("Role not found")
    );
      
    List<User> users = userRepository.findByRole(prevRole);
    users.stream().forEach(u -> u.setRole(newRole));

    userRepository.saveAll(users);

    roleRepository.delete(prevRole);
  }

  public Map<String, Long> getRoleCount() {
    return Map.of("count", roleRepository.count());
  }
}
