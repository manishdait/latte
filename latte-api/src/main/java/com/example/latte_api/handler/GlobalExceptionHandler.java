package com.example.latte_api.handler;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
      new ErrorResponse(
        Instant.now(), 
        HttpStatus.FORBIDDEN.value(),
        "Invalid username or password",
        request.getRequestURI()
      )
    );
  }
  
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
      new ErrorResponse(
        Instant.now(), 
        HttpStatus.BAD_REQUEST.value(),
        e.getMessage(),
        request.getRequestURI()
      )
    );
  }

  @ExceptionHandler({JwtException.class, ExpiredJwtException.class})
  public ResponseEntity<ErrorResponse> handleJwtException(Exception e, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
      new ErrorResponse(
        Instant.now(), 
        HttpStatus.UNAUTHORIZED.value(),
        "Jwt Exception",
        request.getRequestURI()
      )
    );
  }

  @ExceptionHandler(EntityNotFoundException.class) 
  public ResponseEntity<ErrorResponse> handleException(EntityNotFoundException e, HttpServletRequest request) {
    e.printStackTrace();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
      new ErrorResponse(
        Instant.now(), 
        HttpStatus.NOT_FOUND.value(),
        e.getMessage(),
        request.getRequestURI()
      )
    );
  }

  @ExceptionHandler(Exception.class) 
  public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
    e.printStackTrace();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
      new ErrorResponse(
        Instant.now(), 
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Something went wrong",
        request.getRequestURI()
      )
    );
  }
}
