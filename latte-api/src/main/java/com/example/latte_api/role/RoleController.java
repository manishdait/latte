package com.example.latte_api.role;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.latte_api.role.dto.RoleRequest;
import com.example.latte_api.role.dto.RoleResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/latte-api/v1/roles")
@RequiredArgsConstructor
public class RoleController {
  private final RoleService roleService;

  @GetMapping()
  public ResponseEntity<List<RoleResponse>> getRoles() {
    return ResponseEntity.status(HttpStatus.OK).body(roleService.getRoles());
  }

  @PostMapping()
  public ResponseEntity<RoleResponse> createRole (@RequestBody RoleRequest request) {
    return ResponseEntity.status(HttpStatus.OK).body(roleService.createRole(request));
  }
  
}
