package com.example.latte_api.role.authority;

import java.util.List;

import com.example.latte_api.role.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "authority")
public class Authority { 
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "authority_seq_generator")
  @SequenceGenerator(name = "authority_seq_generator", sequenceName = "authority_seq", allocationSize = 1, initialValue = 101)
  @Column(name = "id")
  private Long id;

  @Column(name = "authority", unique = true)
  private String authority;

  @JsonIgnore
  @ManyToMany(mappedBy = "authorities")
  private List<Role> roles;
}
