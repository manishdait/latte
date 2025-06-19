package com.example.latte_api.smtp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Table(name = "smtp_config")
public class SMTPProperties {
  @Id
  @Column(name = "id")
  private Integer id;
  
  @Column(name = "host")
  private String host;
  
  @Column(name = "port")
  private int port;
  
  @Column(name = "username")
  private String username;
  
  @Column(name = "password")
  private String password;
}
