package com.example.latte_api.smtp;

import java.util.Optional;
import java.util.Properties;

import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SMTPService {
  private final SMTPRepository smtpRepository;
  private final ApplicationContext applicationContext;
  private final EmailService emailservice;
  private final AESUtil aesUtil;

  @PostConstruct
  public void init() {
    Optional<SMTPProperties> _properties = smtpRepository.findById(101);
    if (_properties.isEmpty()) {
      return;
    }

    registerMailSender(_properties.get());
  }

  public void configureSMTP(SMTPDto config) {
    SMTPProperties properties = smtpRepository.save(
      SMTPProperties.builder()
        .id(101)
        .host(config.provider().getHost())
        .port(config.provider().getPort())
        .username(aesUtil.encrypt(config.username()))
        .password(aesUtil.encrypt(config.password()))
        .build()
    );

    registerMailSender(properties);
    emailservice.sendMail();
  }

  private void registerMailSender(SMTPProperties properties) {
    String beanName = "javaMailSender";
    JavaMailSenderImpl mailSenderImpl = (JavaMailSenderImpl) applicationContext.getBean(beanName);
    mailSenderImpl.setHost(properties.getHost());
    mailSenderImpl.setPort(properties.getPort());
    mailSenderImpl.setUsername(aesUtil.decrypt(properties.getUsername()));
    mailSenderImpl.setPassword(aesUtil.decrypt(properties.getPassword()));

    Properties props = mailSenderImpl.getJavaMailProperties();
    props.put("mail.smtp", "*");
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.auth", true);
    props.put("mail.smtp.auth", true);
    props.put("mail.smtp.starttls.enable", true);
  }
}
