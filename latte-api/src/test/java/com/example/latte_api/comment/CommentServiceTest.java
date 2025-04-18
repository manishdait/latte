package com.example.latte_api.comment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.latte_api.activity.enums.ActivityType;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.example.latte_api.activity.Activity;
import com.example.latte_api.activity.ActivityService;
import com.example.latte_api.activity.dto.ActivityDto;
import com.example.latte_api.comment.dto.CommentRequest;
import com.example.latte_api.ticket.Ticket;
import com.example.latte_api.ticket.TicketRepository;
import com.example.latte_api.user.User;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
  private CommentService commentService;

  @Mock
  private TicketRepository ticketRepository;

  @Mock
  private ActivityService activityService;

  @Captor
  private ArgumentCaptor<Activity> activityCaptor;

  @BeforeEach
  void setup() {
    commentService = new CommentService(ticketRepository, activityService);
  }

  @AfterEach
  void purge() {
    commentService = null;
  }

  @Test
  void shouldReturn_activityDto_onCreateComment() {
    // mock
    final Ticket ticket = Mockito.mock(Ticket.class);
    final User user = Mockito.mock(User.class);
    final ActivityDto response = Mockito.mock(ActivityDto.class);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final CommentRequest request = new CommentRequest("Message", 101L);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(ticketRepository.findById(request.ticketId())).thenReturn(Optional.of(ticket));
    when(activityService.createActivity(any(Activity.class))).thenReturn(response);
    final ActivityDto result = commentService.createComment(request, authentication);

    // then
    verify(authentication, times(1)).getPrincipal();
    verify(ticketRepository, times(1)).findById(request.ticketId());
    verify(activityService, times(1)).createActivity(any(Activity.class));

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  void shouldThrow_exception_onCreateComment_forInvalidTicketId() {
    // mock
    final User user = Mockito.mock(User.class);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final CommentRequest request = new CommentRequest("Message", 101L);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(ticketRepository.findById(request.ticketId())).thenReturn(Optional.empty());

    // then
    Assertions.assertThatThrownBy(() -> commentService.createComment(request, authentication))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldThrow_exception_onCreateComment_forLockTicket() {
    // mock
    final User user = Mockito.mock(User.class);
    final Ticket ticket = Mockito.mock(Ticket.class);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final CommentRequest request = new CommentRequest("Message", 101L);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(ticketRepository.findById(request.ticketId())).thenReturn(Optional.of(ticket));
    when(ticket.getLock()).thenReturn(true);

    // then
    Assertions.assertThatThrownBy(() -> commentService.createComment(request, authentication))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldReturn_activityDto_whenUpdateTicket() {
    // mock
    final ActivityDto activityDto = Mockito.mock(ActivityDto.class);

    final User user = Mockito.mock(User.class);
    final Ticket ticket = Mockito.mock(Ticket.class);
    final Activity activity = new Activity(101L, ActivityType.COMMENT, "comment", user, ticket);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final long id = 101L;
    final CommentRequest request = new CommentRequest("new_comment", id);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(activityService.getActivity(id)).thenReturn(activity);
    when(ticket.getLock()).thenReturn(false);
    when(user.getEmail()).thenReturn("test@test.in");
    when(activityService.updateActivity(any(Activity.class))).thenReturn(activityDto);

    final ActivityDto result = commentService.updateComment(id, request, authentication);

    verify(authentication, times(1)).getPrincipal();
    verify(activityService, times(1)).getActivity(id);
    verify(ticket, times(1)).getLock();
    verify(activityService, times(1)).updateActivity(any(Activity.class));

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  void shouldThrow_exception_whenUpdateTicket_ticketIsLock() {
    final User user = Mockito.mock(User.class);
    final Ticket ticket = Mockito.mock(Ticket.class);
    final Activity activity = new Activity(101L, ActivityType.COMMENT, "comment", user, ticket);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final long id = 101L;
    final CommentRequest request = new CommentRequest("new_comment", id);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(activityService.getActivity(id)).thenReturn(activity);
    when(ticket.getLock()).thenReturn(true);

    Assertions.assertThatThrownBy(() -> commentService.updateComment(id, request, authentication))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldThrow_exception_whenUpdateTicket_userNotOwner() {
    final User user = Mockito.mock(User.class);
    final Ticket ticket = Mockito.mock(Ticket.class);
    final Activity activity = new Activity(101L, ActivityType.COMMENT, "comment", User.builder().email("noowner@test.in").build(), ticket);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final long id = 101L;
    final CommentRequest request = new CommentRequest("new_comment", id);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(activityService.getActivity(id)).thenReturn(activity);
    when(ticket.getLock()).thenReturn(false);

    Assertions.assertThatThrownBy(() -> commentService.updateComment(id, request, authentication))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrow_exception_whenUpdateTicket_ifNotComment() {
    final User user = Mockito.mock(User.class);
    final Ticket ticket = Mockito.mock(Ticket.class);
    final Activity activity = new Activity(101L, ActivityType.EDIT, "comment", User.builder().email("noowner@test.in").build(), ticket);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final long id = 101L;
    final CommentRequest request = new CommentRequest("new_comment", id);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(activityService.getActivity(id)).thenReturn(activity);
    when(ticket.getLock()).thenReturn(false);

    Assertions.assertThatThrownBy(() -> commentService.updateComment(id, request, authentication))
            .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrow_exception_whenUpdateTicket_InvalidTicketId() {
    final User user = Mockito.mock(User.class);
    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final long id = 101L;
    final CommentRequest request = new CommentRequest("new_comment", id);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(activityService.getActivity(id)).thenThrow(new EntityNotFoundException("Activity not found"));

    Assertions.assertThatThrownBy(() -> commentService.updateComment(id, request, authentication))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldReturn_activityDto_whenDeleteTicket() {
    // mock
    final User user = Mockito.mock(User.class);
    final Ticket ticket = Mockito.mock(Ticket.class);
    final Activity activity = new Activity(101L, ActivityType.COMMENT, "comment", user, ticket);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final long id = 101L;

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(activityService.getActivity(id)).thenReturn(activity);
    when(ticket.getLock()).thenReturn(false);
    when(user.getEmail()).thenReturn("test@test.in");

    commentService.deleteComment(id, authentication);

    verify(authentication, times(1)).getPrincipal();
    verify(activityService, times(1)).getActivity(id);
    verify(ticket, times(1)).getLock();
  }

  @Test
  void shouldThrow_exception_whenDeleteTicket_ticketLock() {
    // mock
    final User user = Mockito.mock(User.class);
    final Ticket ticket = Mockito.mock(Ticket.class);
    final Activity activity = new Activity(101L, ActivityType.COMMENT, "comment", user, ticket);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final long id = 101L;

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(activityService.getActivity(id)).thenReturn(activity);
    when(ticket.getLock()).thenReturn(true);

    Assertions.assertThatThrownBy(() -> commentService.deleteComment(id, authentication))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldThrow_exception_whenDeleteTicket_userNotOwner() {
    // mock
    final User user = Mockito.mock(User.class);
    final Ticket ticket = Mockito.mock(Ticket.class);
    final Activity activity = new Activity(101L, ActivityType.COMMENT, "comment", User.builder().email("noowner@test.in").build(), ticket);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final long id = 101L;

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(activityService.getActivity(id)).thenReturn(activity);
    when(ticket.getLock()).thenReturn(false);
    when(user.getEmail()).thenReturn("test@test.in");

    Assertions.assertThatThrownBy(() -> commentService.deleteComment(id, authentication))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrow_exception_whenDeleteTicket_notComment() {
    // mock
    final User user = Mockito.mock(User.class);
    final Ticket ticket = Mockito.mock(Ticket.class);
    final Activity activity = new Activity(101L, ActivityType.EDIT, "comment", user, ticket);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final long id = 101L;

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(activityService.getActivity(id)).thenReturn(activity);
    when(ticket.getLock()).thenReturn(false);
    when(user.getEmail()).thenReturn("test@test.in");

    Assertions.assertThatThrownBy(() -> commentService.deleteComment(id, authentication))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrow_exception_whenDeleteTicket_invalidId() {
    // mock
    final User user = Mockito.mock(User.class);
    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final long id = 101L;

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(activityService.getActivity(id)).thenThrow(new EntityNotFoundException("Activity not found"));

    Assertions.assertThatThrownBy(() -> commentService.deleteComment(id, authentication))
      .isInstanceOf(EntityNotFoundException.class);
  }
}
