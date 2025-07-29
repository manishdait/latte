package com.example.latte_api.role.authority;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/*
 * Authority Repository
 */
public interface AuthorityRepository extends JpaRepository<Authority, Long>{
  Optional<Authority> findByAuthorityIgnoreCase(String authority);
}
