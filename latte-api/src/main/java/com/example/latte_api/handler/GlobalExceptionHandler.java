package com.example.latte_api.handler;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.latte_api.exception.OperationNotPermittedException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler({BadCredentialsException.class, OperationNotPermittedException.class})
  public ResponseEntity<ErrorResponse> handleBadCredential(Exception e, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
      new ErrorResponse(
        Instant.now(), 
        HttpStatus.FORBIDDEN.value(),
        e.getMessage(),
        request.getRequestURI()
      )
    );
  }
  
  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ErrorResponse> handleIllegalArgState(Exception e, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
      new ErrorResponse(
        Instant.now(), 
        HttpStatus.BAD_REQUEST.value(),
        e.getMessage(),
        request.getRequestURI()
      )
    );
  }

  @ExceptionHandler(EntityNotFoundException.class) 
  public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException e, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
      new ErrorResponse(
        Instant.now(), 
        HttpStatus.NOT_FOUND.value(),
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
