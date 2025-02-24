package com.example.latte_api.activity.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.latte_api.activity.Activity;
import com.example.latte_api.activity.enums.ActivityType;
import com.example.latte_api.ticket.Ticket;
import com.example.latte_api.ticket.enums.Priority;
import com.example.latte_api.ticket.enums.Status;
import com.example.latte_api.user.User;

public class ActivityGeneratorTest {
  private ActivityGenerator activityGenerator;  

  @BeforeEach
  void setup() {
    activityGenerator = new ActivityGenerator();
  }

  @AfterEach
  void purge() {
    activityGenerator = null;
  }

  @Test
  void shouldReturn_activity_forTicketCreated() {
    // given 
    final User user = User.builder()
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .build();
    
    final Ticket ticket = Ticket.builder()
      .title("Title")
      .description("description")
      .priority(Priority.LOW)
      .status(Status.OPEN)
      .createdBy(user)
      .assignedTo(null)
      .build();

    // when
    final Activity result = activityGenerator.ticketCreated(user, ticket);

    // then
    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result.getAuthor()).isEqualTo(user);
    Assertions.assertThat(result.getTicket()).isEqualTo(ticket);
    Assertions.assertThat(result.getType()).isEqualTo(ActivityType.EDIT);
    Assertions.assertThat(result.getMessage()).isEqualTo("Peter created ticked");
  }

  @Test
  void shouldReturn_activity_forTitleChanged() {
    // given 
    final User user = User.builder()
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .build();
    
    final Ticket ticket = Ticket.builder()
      .title("Title")
      .description("description")
      .priority(Priority.LOW)
      .status(Status.OPEN)
      .createdBy(user)
      .assignedTo(null)
      .build();
    
    final String oldTitle = "Title";
    final String newTitle = "New Title";

    // when
    final Activity result = activityGenerator.titleChanged(user, ticket, oldTitle, newTitle);

    // then
    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result.getAuthor()).isEqualTo(user);
    Assertions.assertThat(result.getTicket()).isEqualTo(ticket);
    Assertions.assertThat(result.getType()).isEqualTo(ActivityType.EDIT);
    Assertions.assertThat(result.getMessage()).isEqualTo("Peter change title from Title to New Title");
  }

  @Test
  void shouldReturn_activity_forDescriptionChanged() {
    // given 
    final User user = User.builder()
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .build();
    
    final Ticket ticket = Ticket.builder()
      .title("Title")
      .description("description")
      .priority(Priority.LOW)
      .status(Status.OPEN)
      .createdBy(user)
      .assignedTo(null)
      .build();

    // when
    final Activity result = activityGenerator.descriptionChanged(user, ticket);

    // then
    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result.getAuthor()).isEqualTo(user);
    Assertions.assertThat(result.getTicket()).isEqualTo(ticket);
    Assertions.assertThat(result.getType()).isEqualTo(ActivityType.EDIT);
    Assertions.assertThat(result.getMessage()).isEqualTo("Peter edited the description of ticket");
  }
  
  @Test
  void shouldReturn_activity_forAssignedToAdded() {
    // given 
    final User user = User.builder()
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .build();
    
    final Ticket ticket = Ticket.builder()
      .title("Title")
      .description("description")
      .priority(Priority.LOW)
      .status(Status.OPEN)
      .createdBy(user)
      .assignedTo(null)
      .build();

    final String old = "";
    final String curr = "Louis";

    // when
    final Activity result = activityGenerator.assignedToChanged(user, ticket, old, curr);

    // then
    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result.getAuthor()).isEqualTo(user);
    Assertions.assertThat(result.getTicket()).isEqualTo(ticket);
    Assertions.assertThat(result.getType()).isEqualTo(ActivityType.EDIT);
    Assertions.assertThat(result.getMessage()).isEqualTo("Peter assigned ticket to Louis");
  }

  @Test
  void shouldReturn_activity_forUnassigned() {
    // given 
    final User user = User.builder()
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .build();
    
    final Ticket ticket = Ticket.builder()
      .title("Title")
      .description("description")
      .priority(Priority.LOW)
      .status(Status.OPEN)
      .createdBy(user)
      .assignedTo(null)
      .build();

    final String old = "Louis";
    final String curr = "";

    // when
    final Activity result = activityGenerator.assignedToChanged(user, ticket, old, curr);

    // then
    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result.getAuthor()).isEqualTo(user);
    Assertions.assertThat(result.getTicket()).isEqualTo(ticket);
    Assertions.assertThat(result.getType()).isEqualTo(ActivityType.EDIT);
    Assertions.assertThat(result.getMessage()).isEqualTo("Peter unassigned Louis");
  }

  @Test
  void shouldReturn_activity_forticketAssignedToOther() {
    // given 
    final User user = User.builder()
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .build();
    
    final Ticket ticket = Ticket.builder()
      .title("Title")
      .description("description")
      .priority(Priority.LOW)
      .status(Status.OPEN)
      .createdBy(user)
      .assignedTo(null)
      .build();

    final String old = "Louis";
    final String curr = "Stewie";

    // when
    final Activity result = activityGenerator.assignedToChanged(user, ticket, old, curr);

    // then
    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result.getAuthor()).isEqualTo(user);
    Assertions.assertThat(result.getTicket()).isEqualTo(ticket);
    Assertions.assertThat(result.getType()).isEqualTo(ActivityType.EDIT);
    Assertions.assertThat(result.getMessage()).isEqualTo("Peter unassigned Louis and assigned ticket to Stewie");
  }

  @Test
  void shouldReturn_activity_forPriorityChanged() {
    // given 
    final User user = User.builder()
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .build();
    
    final Ticket ticket = Ticket.builder()
      .title("Title")
      .description("description")
      .priority(Priority.LOW)
      .status(Status.OPEN)
      .createdBy(user)
      .assignedTo(null)
      .build();

    final Priority oldPriority = Priority.LOW;
    final Priority newPriority = Priority.MEDIUM;

    // when
    final Activity result = activityGenerator.priorityChanged(user, ticket, oldPriority, newPriority);

    // then
    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result.getAuthor()).isEqualTo(user);
    Assertions.assertThat(result.getTicket()).isEqualTo(ticket);
    Assertions.assertThat(result.getType()).isEqualTo(ActivityType.EDIT);
    Assertions.assertThat(result.getMessage()).isEqualTo("Peter change priority from LOW to MEDIUM");
  }

  @Test
  void shouldReturn_activity_forStatusChanged() {
    // given 
    final User user = User.builder()
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .build();
    
    final Ticket ticket = Ticket.builder()
      .title("Title")
      .description("description")
      .priority(Priority.LOW)
      .status(Status.OPEN)
      .createdBy(user)
      .assignedTo(null)
      .build();

    final Status oldStatus = Status.OPEN;
    final Status newStatus = Status.CLOSE;

    // when
    final Activity result = activityGenerator.statusChanged(user, ticket, oldStatus, newStatus);

    // then
    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result.getAuthor()).isEqualTo(user);
    Assertions.assertThat(result.getTicket()).isEqualTo(ticket);
    Assertions.assertThat(result.getType()).isEqualTo(ActivityType.EDIT);
    Assertions.assertThat(result.getMessage()).isEqualTo("Peter change status from OPEN to CLOSE");
  }
}
