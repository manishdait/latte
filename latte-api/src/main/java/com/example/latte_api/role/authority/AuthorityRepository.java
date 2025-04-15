package com.example.latte_api.role.authority;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long>{
  Optional<Authority> findByAuthority(String authority);
}
