package com.example.latte_api.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.latte_api.user.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  Page<Notification> findByUser(User user, Pageable pageable);
}
