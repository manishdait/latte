package com.example.latte_api.role.authority;

import lombok.Getter;

public enum IAuthority {
  CREATE_USER("user::create"),
  EDIT_USER("user::edit"),
  DELETE_USER("user::delete"),
  RESET_USER_PASSWORD("user::reset-password"),
  CREATE_TICKET("ticket::create"),
  EDIT_TICKET("ticket::edit"),
  DELETE_TICKET("ticket::delete"),
  LOCK_TICKET("ticket::lock-unlock"),
  ASSIGN_TICKET("ticket::assign"),
  CREATE_ROLE("role::create"),
  EDIT_ROLE("role::edit"),
  DELETE_ROLE("role::delete");

  @Getter
  private String authority;

  IAuthority(String authority) {
    this.authority = authority;
  }
}