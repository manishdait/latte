package com.example.latte_api.role;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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

/*
 * Role Service
 */
@Service
@RequiredArgsConstructor
public class RoleService {
  private final AuthorityRepository authorityRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  private final RoleMapper roleMapper;

  /**
  * Retrieves a paginated list of roles.
  *
  * @param pageNumber The page number (0-indexed).
  * @param pageSize The number of roles per page.
  * @return A PagedEntity containing a list of RoleResponse objects and pagination information.
  */
  public PagedEntity<RoleResponse> getRoles(int pageNumber, int pageSize) {
    Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Direction.ASC, "id"));
    Page<Role> rolePage =  roleRepository.findAll(pageable);

    PagedEntity<RoleResponse> respone = new PagedEntity<>();
    respone.setNext(rolePage.hasNext());
    respone.setPrevious(rolePage.hasPrevious());
    respone.setTotalElement(rolePage.getTotalElements());
    respone.setContent(rolePage.getContent().stream().map(r -> roleMapper.mapToRoleResponse(r)).toList());

    return respone;
  }

  /**
   * Retrieves a single role by its ID.
   *
   * @param id The ID of the role to retrieve.
   * @return The RoleResponse object representing the found role.
   * @throws EntityNotFoundException if the role with the given ID is not found.
   */
  public RoleResponse getRole(Long id) {
    Role role = findRoleById(id);
    return roleMapper.mapToRoleResponse(role);
  }

  /**
   * Creates a new role.
   *
   * @param request The RoleRequest DTO containing the details for the new role.
   * @return The RoleResponse object representing the newly created role.
   * @throws IllegalArgumentException if a role with the same name already exists.
   * @throws EntityNotFoundException if any specified authority does not exist.
   */
  @Transactional
  public RoleResponse createRole(RoleRequest request) {
    roleRepository.findByRole(request.role()).ifPresent((r) -> {
      throw new IllegalArgumentException("Role '" + request.role() + "' already exists.");
    });

    List<Authority> authorities = new ArrayList<>();

    for (String authority : request.authorities()) {
      authorities.add(findAuthority(authority));
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

  /**
   * Updates an existing role.
   *
   * @param id The ID of the role to update.
   * @param request The RoleRequest DTO containing the updated details for the role.
   * @return The RoleResponse object representing the updated role.
   * @throws EntityNotFoundException if the role or authority is not found.
   * @throws IllegalStateException if the role is not editable.
   * @throws IllegalArgumentException if updated role name already exista.
   */
  @Transactional
  public RoleResponse updateRole(Long id, RoleRequest request) {
    Role role = findRoleById(id);

    if (!role.isEditable()) {
      throw new IllegalStateException("Role '" + role.getRole() + "' cannot be edited.");
    }

    if (request.role() != null && !request.role().equals(role.getRole())) {
      roleRepository.findByRole(request.role()).ifPresent(r -> {
        throw new IllegalArgumentException("Role '" + request.role() + "' already exists.");
      });
      role.setRole(request.role());
    }

    if (request.authorities() != null) {
      List<Authority> authorities = new ArrayList<>();
      for (String authority : request.authorities()) {
        authorities.add(findAuthority(authority));
      }

      role.setAuthorities(authorities);
    }

    roleRepository.save(role);
    return roleMapper.mapToRoleResponse(role);
  }

  /**
   * Deletes a role and reassigns its associated users to a new role.
   *
   * @param roleToDeleteId The ID of the role to be deleted.
   * @param roleToReassignId The ID of the role to which users associated with the deleted role will be reassigned.
   * @throws IllegalArgumentException if the roleToDeleteId and roleToReassignId are the same.
   * @throws EntityNotFoundException if either the role to delete or the new role is not found.
   * @throws IllegalStateException if the role to be deleted is not deletable.
   */
  @Transactional
  public void deleteRole(Long roleToDeleteId, Long roleToReassignId) {
    if (roleToDeleteId.equals(roleToReassignId)) {
      throw new IllegalArgumentException("Cannot delete role and reassign users to the same role");
    }

    Role prevRole = findRoleById(roleToDeleteId);

    if(!prevRole.isDeletable()) {
      throw new IllegalStateException("Role '" + prevRole.getRole() + "' cannot be deleted.");
    }

    Role newRole = findRoleById(roleToReassignId);
      
    List<User> users = userRepository.findByRole(prevRole);
    users.stream().forEach(u -> u.setRole(newRole));

    userRepository.saveAll(users);

    roleRepository.delete(prevRole);
  }

  /**
   * Helper method to find a Role by its ID.
   * 
   * @param roleId The ID of the role to find.
   * @return The found Role entity.
   * @throws EntityNotFoundException If the role is not found.
   */
  private Role findRoleById(Long roleId) {
    return roleRepository.findById(roleId).orElseThrow(
      () -> new EntityNotFoundException("Role not found with ID: " + roleId)
    );
  }

  /**
   * Helper method to find a Authority.
   * 
   * @param authority The name of the authority to find.
   * @return The found Authority entity.
   * @throws EntityNotFoundException If the authority is not found.
   */
  private Authority findAuthority(String authority) {
    return authorityRepository.findByAuthorityIgnoreCase(authority).orElseThrow(
      () -> new EntityNotFoundException("Authority '" + authority + "' does not exist.")
    );
  }
}
