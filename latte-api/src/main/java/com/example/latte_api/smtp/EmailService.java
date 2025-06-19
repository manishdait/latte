package com.example.latte_api.smtp;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
  private final JavaMailSender mailSender;

  @Async
  public void sendMail() {
    MimeMessage mimeMessage = mailSender.createMimeMessage();
    MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);

    try {
      messageHelper.setTo("daitmanish24@gmail.com");
      messageHelper.setFrom("daitmanish88@gmail.com");
      messageHelper.setSubject("Test mail");
      messageHelper.setText("SMTP config works");
      mailSender.send(mimeMessage);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
