package com.example.latte_api.notification;

import java.time.Instant;

public record NotificationDto(Long id, String message, Instant timestamp) {}
