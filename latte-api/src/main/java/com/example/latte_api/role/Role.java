package com.example.latte_api.role;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.example.latte_api.role.authority.Authority;
import com.example.latte_api.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
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
@Table(name = "role")
public class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq_generator")
  @SequenceGenerator(name = "role_seq_generator", sequenceName = "role_seq", initialValue = 101, allocationSize = 1)
  @Column(name = "id")
  private Long id;

  @Column(name = "role", unique = true)
  private String role;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
    name = "role_authority",
    joinColumns = {@JoinColumn(name = "role_id")},
    inverseJoinColumns = {@JoinColumn(name = "authority_id")}
  )
  private List<Authority> authorities;

  @JsonIgnore
  @OneToMany(mappedBy = "role")
  private List<User> users;

  public List<SimpleGrantedAuthority> getAuthorities() {
    return authorities.stream()
      .map(a -> new SimpleGrantedAuthority(a.getAuthority()))
      .toList();
  }
}
