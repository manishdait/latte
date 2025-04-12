package com.example.latte_api.comment;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.latte_api.activity.Activity;
import com.example.latte_api.activity.ActivityService;
import com.example.latte_api.activity.dto.ActivityDto;
import com.example.latte_api.activity.enums.ActivityType;
import com.example.latte_api.comment.dto.CommentRequest;
import com.example.latte_api.ticket.Ticket;
import com.example.latte_api.ticket.TicketRepository;
import com.example.latte_api.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
  private final TicketRepository ticketRepository;
  
  private final ActivityService activityService;

  public ActivityDto createComment(CommentRequest request, Authentication authentication){
    User user = (User) authentication.getPrincipal();

    Ticket ticket = ticketRepository.findById(request.ticketId()).orElseThrow();

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
    if (!activity.getAuthor().getEmail().equals(user.getEmail()) && !activity.getType().equals(ActivityType.COMMENT)) {
      return;
    }
    activityService.deleteActivity(activity);
  }

  public ActivityDto updateComment(Long id, CommentRequest request, Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    Activity activity = activityService.getActivity(id);
    
    if (!activity.getAuthor().getEmail().equals(user.getEmail()) && !activity.getType().equals(ActivityType.COMMENT)) {
      throw new IllegalArgumentException("Opretion not permited");
    }

    activity.setMessage(request.message());
    return activityService.updateActivity(activity);
  }
}
