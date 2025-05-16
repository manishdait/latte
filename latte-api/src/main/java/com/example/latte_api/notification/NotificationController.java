package com.example.latte_api.notification;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.latte_api.shared.PagedEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/latte-api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<PagedEntity<NotificationDto>> getUserNotification(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size,  Authentication authentication) {
    return ResponseEntity.status(HttpStatus.OK).body(notificationService.getUserNotification(page, size, authentication));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, Boolean>> deleteNotification(@PathVariable Long id, Authentication authentication) {
    notificationService.deleteNotification(id, authentication);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("deleted", true));
  }
}
