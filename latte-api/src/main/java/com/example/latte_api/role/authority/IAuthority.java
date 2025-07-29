package com.example.latte_api.role.authority;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

/*
 * Authorities
 */
public enum IAuthority {
  // User Management
  CREATE_USER("user::create"),
  EDIT_USER("user::edit"),
  DELETE_USER("user::delete"),
  RESET_USER_PASSWORD("user::reset-password"),

  // Ticket Management
  CREATE_TICKET("ticket::create"),
  EDIT_TICKET("ticket::edit"),
  DELETE_TICKET("ticket::delete"),
  LOCK_TICKET("ticket::lock-unlock"),
  ASSIGN_TICKET("ticket::assign"),

  // Role Management
  CREATE_ROLE("role::create"),
  EDIT_ROLE("role::edit"),
  DELETE_ROLE("role::delete"),

  // Client Management
  CREATE_CLIENT("client::create"),
  EDIT_CLIENT("client::edit"),
  DELETE_CLIENT("client::delete");

  @Getter
  private String authority;

  IAuthority(String authority) {
    this.authority = authority;
  }

  /*
   * Get all authorities
   */
  public List<IAuthority> getAllAuthorities() {
    return Arrays.asList(IAuthority.values());
  }
}