package com.example.latte_api.exception;

public class OperationNotPermittedException extends RuntimeException { 
  public OperationNotPermittedException() {
    super("Operation not permitted");
  }
}
