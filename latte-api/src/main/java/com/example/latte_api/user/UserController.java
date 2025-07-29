package com.example.latte_api.user;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.latte_api.security.PasswordService;
import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.user.dto.ResetPasswordRequest;
import com.example.latte_api.user.dto.UserRequest;
import com.example.latte_api.user.dto.UserResponse;

import lombok.RequiredArgsConstructor;

/*
 * User Controller
 */
@RestController
@RequestMapping("/latte-api/v1/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;
  private final PasswordService passwordService;

  /*
   * Retrieves a paginated list of all users with full details.
   */
  @GetMapping()
  public ResponseEntity<PagedEntity<UserResponse>> getUsers(
    @RequestParam(defaultValue = "0") int pageNumber, 
    @RequestParam(defaultValue = "10") int pageSize
  ) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.getUsers(pageNumber, pageSize));
  }

  /*
   * Retrieves a paginated list containing only the first names of users.
   */
  @GetMapping("/names")
  public ResponseEntity<PagedEntity<String>> getPagedUserFirstname(
    @RequestParam(defaultValue = "0") int pageNumber, 
    @RequestParam(defaultValue = "10") int pageSize
  ) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.getPagedUserFirstnames(pageNumber, pageSize));
  }

  /*
   * Retrieves the details of the currently authenticated user.
   */
  @GetMapping("/me")
  public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.getCurrentUser(authentication));
  }

  /*
   * Retrieves the details of a specific user by their email address.
   */
  @GetMapping("/{userEmail}")
  public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String userEmail) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByEmail(userEmail));
  }

  /*
   * Updates the details of the currently authenticated user.
   */
  @PutMapping("/me")
  public ResponseEntity<UserResponse> updateCurrentUser(
    @RequestBody UserRequest request, 
    Authentication authentication
  ) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.updateCurrentUser(request, authentication));
  }

  /*
   * Updates the details of a specific user by their email address.
   */
  @PutMapping("/{userEmail}")
  public ResponseEntity<UserResponse> updateUserByEmail(
    @RequestBody UserRequest request, 
    @PathVariable String userEmail
  ) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserByEmail(request, userEmail));
  }

  /*
   * Resets the password for the currently authenticated user.
   */
  @PatchMapping("/me/password")
  public ResponseEntity<UserResponse> resetCurrentUserPassword(
    @RequestBody ResetPasswordRequest request, 
    Authentication authentication
  ) {
    return ResponseEntity.status(HttpStatus.OK).body(passwordService.resetPassword(request, authentication));
  }

  /*
   * Resets the password for a specific user by their email address.
   */
  @PatchMapping("/{userEmail}/password")
  public ResponseEntity<UserResponse> resetUserPasswordByEmail(
    @RequestBody ResetPasswordRequest request, 
    @PathVariable String userEmail
  ) {
    return ResponseEntity.status(HttpStatus.OK).body(passwordService.resetPassword(request, userEmail));
  }

  /*
   * Deletes a user by their email address.
   */
  @DeleteMapping("/{userEmail}")
  public ResponseEntity<Map<String, Object>> deleteUserByEmail(@PathVariable String userEmail) {
    userService.deleteUserByEmail(userEmail);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("key", userEmail, "deleted", true));
  }
}
