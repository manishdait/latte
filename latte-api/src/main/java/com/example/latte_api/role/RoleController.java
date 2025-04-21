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


@RestController
@RequestMapping("/latte-api/v1/roles")
@RequiredArgsConstructor
public class RoleController {
  private final RoleService roleService;

  @GetMapping()
  public ResponseEntity<PagedEntity<RoleResponse>> getRoles(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.OK).body(roleService.getRoles(page, size));
  }

  @GetMapping("/{id}")
  public ResponseEntity<RoleResponse> getRole(@PathVariable Long id) {
      return ResponseEntity.status(HttpStatus.OK).body(roleService.getRole(id));
  }
  

  @GetMapping("/count")
  public ResponseEntity<Map<String, Long>> getRoleCount() {
    return ResponseEntity.status(HttpStatus.OK).body(roleService.getRoleCount());
  }
  

  @PostMapping()
  public ResponseEntity<RoleResponse> createRole(@RequestBody RoleRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(request));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<RoleResponse> updateRole(@PathVariable Long id, @RequestBody RoleRequest request) {
    return ResponseEntity.status(HttpStatus.OK).body(roleService.editResponse(id, request));
  }

  @DeleteMapping("/{id}/update-to/{newId}")
  public ResponseEntity<Map<String, Boolean>> deleteRole(@PathVariable Long id, @PathVariable Long newId) {
    roleService.deleteRole(id, newId);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("deleted", true));
  }
}
