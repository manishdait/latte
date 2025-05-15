package com.example.latte_api.notification;

import java.time.Instant;

import com.example.latte_api.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "notification")
public class Notification {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_seq_generator")
  @SequenceGenerator(name = "notification_seq_generator", sequenceName = "notification_seq", allocationSize = 1, initialValue = 101)
  private Long id;
  private String message;
  private Instant timestamp;
  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;
}
