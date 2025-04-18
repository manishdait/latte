package com.example.latte_api.user;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.latte_api.role.Role;
import com.example.latte_api.role.authority.IAuthority;
import com.example.latte_api.shared.AbstractAuditingEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "_user")
public class User extends AbstractAuditingEntity implements UserDetails, Principal {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "user_seq_gen")
  @SequenceGenerator(name = "user_seq_gen", sequenceName = "user_seq", initialValue = 101, allocationSize = 1)
  @Column(name = "id")
  private Long id;

  @Column(name = "first_name", unique = true)
  private String firstname;

  @Column(name = "email", unique = true)
  private String email;

  @Column(name = "password")
  private String password;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "role_id")
  private Role role;

  @Column(name = "deletable")
  private boolean deletable;

  @Column(name = "editable")
  private boolean editable;

  @Override
  public String getName() {
    return this.email;
  } 

  @Override
  public String getUsername() {
    return this.email;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<SimpleGrantedAuthority> authorities = new ArrayList<>(this.role.getAuthorities());
    authorities.add(new SimpleGrantedAuthority(this.role.getRole()));
    return authorities;
  }

  public boolean hasAuthority(IAuthority authority) {
    return hasAuthority(authority.getAuthority());
  }

  public boolean hasAuthority(String authority) {
    for (GrantedAuthority _authority : getAuthorities()) {
      if (_authority.getAuthority().equals(authority)) {
        return true;
      }
    }
    return false;
  }
}
