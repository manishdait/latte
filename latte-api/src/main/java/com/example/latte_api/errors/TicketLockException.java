package com.example.latte_api.errors;

import lombok.Getter;
import lombok.Setter;

public class TicketLockException extends RuntimeException {
  @Getter
  @Setter
  private String message;

  public TicketLockException() {
    super("Operation cannot be perform on lock ticket");
    this.message = "Operation cannot be perform on lock ticket";
  }
}
