package com.example.latte_api.error;

public class OperationNotPermittedException extends RuntimeException { 
  public OperationNotPermittedException() {
    super("Operation not permitted");
  }
}
