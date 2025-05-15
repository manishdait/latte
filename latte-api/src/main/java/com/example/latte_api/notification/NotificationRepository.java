package com.example.latte_api.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.latte_api.user.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findByUser(User user);
}
