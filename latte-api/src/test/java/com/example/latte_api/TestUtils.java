package com.example.latte_api;

import java.util.Arrays;
import java.util.List;

import com.example.latte_api.role.Role;
import com.example.latte_api.role.authority.Authority;
import com.example.latte_api.user.User;

/**
 * Utility class for creating and providing test data for unit and integration tests.
 */
public class TestUtils {
  // Users
  public static final String TEST_FIRSTNAME_ADMIN = "Admin";
  public static final String TEST_FIRSTNAME_PETER = "Peter";
  public static final String TEST_FIRSTNAME_LOUIS = "Louis";
  public static final String TEST_FIRSTNAME_STEWIE = "Stewie";

  public static final String TEST_EMAIL_ADMIN = "admin@dev.in";
  public static final String TEST_EMAIL_PETER = "peter@dev.in";
  public static final String TEST_EMAIL_LOUIS = "louis@dev.in";
  public static final String TEST_EMAIL_STEWIE = "stewie@dev.in";

  public static final User createTestUserAdmin() {
    return User.builder()
      .firstname(TEST_FIRSTNAME_ADMIN)
      .email(TEST_EMAIL_ADMIN)
      .password("Password@123")
      .role(createAdminRole())
      .build();
  }

  public static final User createTestUserPeter() {
    return User.builder()
      .firstname(TEST_FIRSTNAME_PETER)
      .email(TEST_EMAIL_PETER)
      .password("Password@123")
      .role(createUserRole())
      .build();
  }

  public static final User createTestUserLouis() {
    return User.builder()
      .firstname(TEST_FIRSTNAME_LOUIS)
      .email(TEST_EMAIL_LOUIS)
      .password("Password@123")
      .role(createUserRole())
      .build();
  }

  public static final User createTestUserStewie() {
    return User.builder()
      .firstname(TEST_FIRSTNAME_STEWIE)
      .email(TEST_EMAIL_STEWIE)
      .password("Password@123")
      .role(createUserRole())
      .build();
  }

  // Authorities
  public static final String TEST_AUTHORITY_READ = "test::read";
  public static final String TEST_AUTHORITY_WRITE = "test::write";
  public static final String TEST_AUTHORITY_DELETE = "test::delete";

  public static final Authority createAuthority(String authority) {
    return  Authority.builder()
      .authority(authority)
      .build();
  }

  public static final Authority createAuthority(Long id, String authority) {
    return  Authority.builder()
      .id(id)
      .authority(authority)
      .build();
  }


  // Roles
  public static final Role createRole(String role, Authority... authorities) {
    return Role.builder()
      .role(role)
      .authorities(Arrays.asList(authorities))
      .editable(true)
      .deletable(true)
      .build();
  }

  public static final Role createRole(String role, boolean deletable, boolean editable, Authority... authorities) {
    return Role.builder()
      .role(role)
      .authorities(Arrays.asList(authorities))
      .editable(deletable)
      .deletable(editable)
      .build();
  }

  public static final Role createAdminRole() {
    return Role.builder()
      .id(101L)
      .role("Admin")
      .authorities(List.of(
        createAuthority(101L, TEST_AUTHORITY_READ),
        createAuthority(102L, TEST_AUTHORITY_WRITE),
        createAuthority(103L, TEST_AUTHORITY_DELETE)
      ))
      .deletable(false)
      .editable(false)
      .build();
  }

  public static final Role createUserRole() {
    return Role.builder()
      .id(101L)
      .role("User")
      .authorities(List.of(createAuthority(101L, TEST_AUTHORITY_READ)))
      .deletable(true)
      .editable(true)
      .build();
  } 
}
