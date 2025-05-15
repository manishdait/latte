package com.example.latte_api.notification;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/latte-api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<List<NotificationDto>> getUserNotification(Authentication authentication) {
    return ResponseEntity.status(HttpStatus.OK).body(notificationService.getUserNotification(authentication));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, Boolean>> deleteNotification(@PathVariable Long id, Authentication authentication) {
    notificationService.deleteNotification(id, authentication);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("deleted", true));
  }
}
