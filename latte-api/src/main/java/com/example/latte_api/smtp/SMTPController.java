package com.example.latte_api.smtp;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/latte-api/v1/smtp")
@RequiredArgsConstructor
public class SMTPController {
  private final SMTPService smtpService;
  
  @PostMapping
  public ResponseEntity<Void> configureSMTP(@RequestBody SMTPDto config) {
    smtpService.configureSMTP(config);
    return ResponseEntity.status(HttpStatus.OK).body(null);
  }
}
