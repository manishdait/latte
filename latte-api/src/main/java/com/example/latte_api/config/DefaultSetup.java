package com.example.latte_api.config;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;
import com.example.latte_api.user.role.Role;
import com.example.latte_api.user.role.RoleRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile({"!test"})
public class DefaultSetup {
  private final RoleRepository roleRepository; 
  private final UserRepository userRepository;

  private final PasswordEncoder encoder;

  @PostConstruct
  @Transactional
  public void init() {
    long count = userRepository.count();

    if (count != 0) {
      return;
    }

    Path dir = Paths.get("data");
    try {
      if(!Files.exists(dir)) {
        Files.createDirectories(dir);
      }
      File cred = new File("data/.cred");

      if (!cred.exists()) {
        cred.createNewFile();
      }

      String password = UUID.randomUUID().toString() + LocalTime.now().hashCode();
  
      FileWriter fileWriter = new FileWriter(cred);
      fileWriter.append(password + "\n");
      fileWriter.close();
        
      cred.setReadOnly();
  
      Role roleAdmin = roleRepository.findByRole("ROLE_ADMIN").orElseThrow();
      User admin = User.builder()
        .firstname("Admin")
        .email("admin@admin.com")
        .password(encoder.encode(password))
        .role(roleAdmin)
        .editable(true)
        .deletable(false)
        .build();
  
      userRepository.save(admin);
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception occurs during initializing default admin user: {}", e.getMessage());
      throw new RuntimeException("Exception creating admin user");
    }
  }
}
