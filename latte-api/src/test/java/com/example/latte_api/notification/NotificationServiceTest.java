package com.example.latte_api.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;

import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.user.User;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {
  private NotificationService notificationService;

  @Mock
  private SimpMessagingTemplate messagingTemplate;
  
  @Mock
  private NotificationRepository notificationRepository;

  @Captor
  private ArgumentCaptor<Notification> notificationCaptor;

  @BeforeEach
  void setup() {
    notificationService = new NotificationService(notificationRepository, messagingTemplate);
  }

  @AfterEach
  void purge() {
    notificationService = null;
  }

  @Test
  void shouldSend_notificationTo_user() {
    final Notification notification = Mockito.mock(Notification.class);

    final User user = Mockito.mock(User.class);
    final String message = "Test notification";

    when(user.getUsername()).thenReturn("peter@test.in");
    when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
    when(notification.getId()).thenReturn(101L);

    notificationService.sendNotification(user, message);

    
    verify(user, times(1)).getFirstname();
    verify(notificationRepository, times(1)).save(notificationCaptor.capture());
    verify(messagingTemplate, times(1))
      .convertAndSendToUser(eq(user.getUsername()), eq("/notification"), any(NotificationDto.class));

    Notification capture = notificationCaptor.getValue();
    Assertions.assertThat(capture.getMessage()).isEqualTo(message);
    Assertions.assertThat(capture.getUser()).isEqualTo(user);
  }

  @Test
  void shouldGet_notificationoF_user() {
    final User user = Mockito.mock(User.class);
    @SuppressWarnings("unchecked")
    final Page<Notification> page = Mockito.mock(Page.class);
    final List<Notification> notifications = List.of(
      new Notification(101L, "message1", Instant.now(), user),
      new Notification(102L, "message2", Instant.now(), user)
    );
    final Pageable pageable = PageRequest.of(0, 2);

    final Authentication authentication = Mockito.mock(Authentication.class);
    final int pageNumber = 0;
    final int size = 2;

    when(authentication.getPrincipal()).thenReturn(user);
    when(notificationRepository.findByUser(user, pageable)).thenReturn(page);
    when(page.hasNext()).thenReturn(false);
    when(page.hasPrevious()).thenReturn(false);
    when(page.getContent()).thenReturn(notifications);

    final PagedEntity<NotificationDto> result = notificationService.getUserNotification(pageNumber, size, authentication);

    verify(authentication, times(1)).getPrincipal();
    verify(notificationRepository, times(1)).findByUser(user, pageable);
    
    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result.getContent()).hasSize(2);
  }

  @Test
  void shouldDelete_notificationById() {
    final Notification notification = Mockito.mock(Notification.class);
    final User user = Mockito.mock(User.class);

    final Authentication authentication = Mockito.mock(Authentication.class);
    final Long id = 101L;

    when(authentication.getPrincipal()).thenReturn(user);
    when(notificationRepository.findById(id)).thenReturn(Optional.of(notification));
    when(notification.getUser()).thenReturn(user);
    when(user.getUsername()).thenReturn("peter@test.in");

    notificationService.deleteNotification(id, authentication);

    verify(authentication, times(1)).getPrincipal();
    verify(notificationRepository, times(1)).delete(notification);
  }
}
