package com.example.latte_api.client;

import java.util.List;

import com.example.latte_api.shared.AbstractAuditingEntity;
import com.example.latte_api.ticket.Ticket;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "client")
public class Client extends AbstractAuditingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "client_seq_generator")
  @SequenceGenerator(name = "client_seq_generator", sequenceName = "client_seq", allocationSize = 1, initialValue = 101)
  @Column(name = "id")
  private Long id;

  @Column(name = "name", unique = true)
  private String name;

  @Column(name = "email", unique = true)
  private String email;

  @Column(name = "phone")
  private String phone;

  private boolean deletable;

  @JsonIgnore
  @OneToMany(mappedBy="client", cascade=CascadeType.REMOVE)
  public List<Ticket> tickets;
}
