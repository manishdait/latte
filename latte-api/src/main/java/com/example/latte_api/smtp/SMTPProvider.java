package com.example.latte_api.smtp;

import lombok.Getter;

public enum SMTPProvider {
  GMAIL("smtp.gmail.com", 587);

  @Getter
  private String host;
  @Getter
  private int port;

  SMTPProvider(String host, int port) {
    this.host = host;
    this.port = port;
  }
}