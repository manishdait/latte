package com.example.latte_api.comment;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.latte_api.activity.Activity;
import com.example.latte_api.activity.ActivityService;
import com.example.latte_api.activity.dto.ActivityDto;
import com.example.latte_api.activity.enums.ActivityType;
import com.example.latte_api.comment.dto.CommentDto;
import com.example.latte_api.ticket.Ticket;
import com.example.latte_api.ticket.TicketRepository;
import com.example.latte_api.user.User;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
  private final TicketRepository ticketRepository;
  
  private final ActivityService activityService;

  public ActivityDto createComment(CommentDto request, Authentication authentication){
    User user = (User) authentication.getPrincipal();

    Ticket ticket = ticketRepository.findById(request.ticketId()).orElseThrow(
      () -> new EntityNotFoundException("Ticket not found")
    );

    if (ticket.getLock()) {
      throw new IllegalStateException("Ticket is locked");
    }

    Activity comment = Activity.builder()
      .type(ActivityType.COMMENT)
      .message(request.message())
      .ticket(ticket)
      .author(user)
      .build();
      
    return activityService.createActivity(comment);
  }

  public void deleteComment(Long id, Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    Activity activity = activityService.getActivity(id);
    
    if (activity.getTicket().getLock()) {
      throw new IllegalStateException("Ticket is locked");
    }

    if (!activity.getAuthor().getEmail().equals(user.getEmail()) || !activity.getType().equals(ActivityType.COMMENT)) {
      throw new IllegalArgumentException("Operation not permitted");
    }
    activityService.deleteActivity(activity);
  }

  public ActivityDto updateComment(Long id, CommentDto request, Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    Activity activity = activityService.getActivity(id);

    if (activity.getTicket().getLock()) {
      throw new IllegalStateException("Ticket is locked");
    }
    
    if (!activity.getAuthor().getEmail().equals(user.getEmail()) || !activity.getType().equals(ActivityType.COMMENT)) {
      throw new IllegalArgumentException("Operation not permitted");
    }

    activity.setMessage(request.message());
    return activityService.updateActivity(activity);
  }
}
