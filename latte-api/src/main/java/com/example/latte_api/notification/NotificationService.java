package com.example.latte_api.notification;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.latte_api.exception.OperationNotPermittedException;
import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.user.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
  private final NotificationRepository notificationRepository;
  private final SimpMessagingTemplate messagingTemplate;

  @Transactional
  public void sendNotification(User user, String message) {
    log.info("Sending notification to `{}` with payload `{}`", user.getFirstname(), message);

    Notification notification = notificationRepository.save(
      Notification.builder()
        .message(message)
        .user(user)
        .timestamp(Instant.now())
        .build()
    );

    messagingTemplate.convertAndSendToUser(
      user.getUsername(),
      "/notification",
      new NotificationDto(
        notification.getId(), 
        notification.getMessage(), 
        notification.getTimestamp()
      )
    );
  }

  public PagedEntity<NotificationDto> getUserNotification(int page, int size, Authentication authentication) {
    User user = (User) authentication.getPrincipal();

    Pageable pageable = PageRequest.of(page, size);
    Page<Notification> notifications = notificationRepository.findByUser(user, pageable);

    PagedEntity<NotificationDto> response = new PagedEntity<>();
    response.setNext(notifications.hasNext());
    response.setPrev(notifications.hasPrevious());
    response.setContent(
      notifications.getContent().stream()
        .map(n -> new NotificationDto(n.getId(), n.getMessage(), n.getTimestamp()))
        .toList()
    );

    return response;
  }

  public void deleteNotification(Long id, Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    Notification notification = notificationRepository.findById(id).orElseThrow();

    if (!notification.getUser().getUsername().equals(user.getUsername())) {
      throw new OperationNotPermittedException();
    }

    notificationRepository.delete(notification);
  }
}
