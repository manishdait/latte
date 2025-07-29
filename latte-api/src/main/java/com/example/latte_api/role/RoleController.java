package com.example.latte_api.role;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.latte_api.role.dto.RoleRequest;
import com.example.latte_api.role.dto.RoleResponse;
import com.example.latte_api.shared.PagedEntity;

import lombok.RequiredArgsConstructor;

/*
 * Role Controller
 */
@RestController
@RequestMapping("/latte-api/v1/roles")
@RequiredArgsConstructor
public class RoleController {
  private final RoleService roleService;

  /*
   *  Retrieves a paginated list of roles.
   */
  @GetMapping()
  public ResponseEntity<PagedEntity<RoleResponse>> getRoles(
    @RequestParam(defaultValue = "0") int pageNumber, 
    @RequestParam(defaultValue = "10") int pageSize
  ) {
    return ResponseEntity.status(HttpStatus.OK).body(roleService.getRoles(pageNumber, pageSize));
  }

  /*
   * Retrieves a single role by its ID.
   */
  @GetMapping("/{id}")
  public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
    return ResponseEntity.status(HttpStatus.OK).body(roleService.getRole(id));
  }

  /*
   * Creates a new role.
   */
  @PostMapping()
  public ResponseEntity<RoleResponse> createRole(@RequestBody RoleRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(request));
  }

  /*
   * Updates an existing role by its ID.
   */
  @PatchMapping("/{id}")
  public ResponseEntity<RoleResponse> updateRole(
    @PathVariable Long id, 
    @RequestBody RoleRequest request
  ) {
    return ResponseEntity.status(HttpStatus.OK).body(roleService.updateRole(id, request));
  }

  /*
   * Deletes a role by its ID and reassigns any associated users to a specified new role.
   */
  @DeleteMapping("/{roleToDeleteId}/update-to/{roleToReassignId}")
  public ResponseEntity<Map<String, Boolean>> deleteRole(
    @PathVariable Long roleToDeleteId, 
    @PathVariable Long roleToReassignId
  ) {
    roleService.deleteRole(roleToDeleteId, roleToReassignId);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("deleted", true));
  }
}
