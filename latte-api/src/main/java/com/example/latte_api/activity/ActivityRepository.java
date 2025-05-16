package com.example.latte_api.activity;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.latte_api.user.User;


public interface ActivityRepository extends JpaRepository<Activity, Long> {
  @Query("select a from Activity a where ticket.id = :ticketId")
  Page<Activity> findActivitesForTicket(@Param("ticketId") Long ticketId, Pageable pageable);
  List<Activity> findByAuthor(User author);
}
