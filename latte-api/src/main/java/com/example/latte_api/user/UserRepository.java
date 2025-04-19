package com.example.latte_api.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.latte_api.role.Role;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmailOrFirstname(String email, String firstname);
  Optional<User> findByFirstname(String firstname);
  Optional<User> findByEmail(String email);
  List<User> findByRole(Role role);
}
