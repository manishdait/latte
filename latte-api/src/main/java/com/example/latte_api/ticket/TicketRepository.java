package com.example.latte_api.ticket;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.latte_api.user.User;
import com.example.latte_api.ticket.enums.Status;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
  Page<Ticket> findByCreatedBy(Pageable pageable, User createdBy);
  Page<Ticket> findByStatus(Status status, Pageable pageable);
  
  List<Ticket> findByCreatedBy(User createdBy);
  List<Ticket> findByAssignedTo(User assignedTo);
}
