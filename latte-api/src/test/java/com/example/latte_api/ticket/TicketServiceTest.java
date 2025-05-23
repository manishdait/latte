package com.example.latte_api.ticket;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import com.example.latte_api.activity.Activity;
import com.example.latte_api.activity.ActivityService;
import com.example.latte_api.activity.utils.ActivityGenerator;
import com.example.latte_api.client.ClientRepository;
import com.example.latte_api.exception.OperationNotPermittedException;
import com.example.latte_api.notification.NotificationService;
import com.example.latte_api.role.Role;
import com.example.latte_api.role.authority.Authority;
import com.example.latte_api.role.authority.IAuthority;
import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.ticket.dto.TicketPatchRequest;
import com.example.latte_api.ticket.dto.TicketRequest;
import com.example.latte_api.ticket.dto.TicketResponse;
import com.example.latte_api.ticket.enums.Priority;
import com.example.latte_api.ticket.enums.Status;
import com.example.latte_api.ticket.mapper.TicketMapper;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {
  private TicketService ticketService;

  @Mock
  private TicketRepository ticketRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ClientRepository clientRepository;

  @Mock
  private TicketMapper ticketMapper;

  @Mock
  private ActivityGenerator activityGenerator;

  @Mock
  private ActivityService activityService;

  @Mock
  private NotificationService notificationService;

  @Captor
  ArgumentCaptor<Ticket> ticketCaptor;

  @BeforeEach
  void setup() {
    ticketService = new TicketService(ticketRepository, userRepository, clientRepository, ticketMapper, activityGenerator, activityService, notificationService);
  }

  @AfterEach
  void purge() {
    ticketService = null;
  }

  @Test
  void shouldReturn_ticketResponse_onCreateTicket_withNoAssignee() {
    // mock
    final User user = User.builder()
      .id(101L)
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .role(Role.builder().role("User").authorities(List.of(Authority.builder().authority("ticket::create").build())).build())
      .build();
    final Activity activity = Mockito.mock(Activity.class);
    final TicketResponse ticketResponse = Mockito.mock(TicketResponse.class);
  
    // given
    final TicketRequest request = new TicketRequest("Test 1", "Test ticket", Priority.LOW, Status.OPEN, "", null);
    final Authentication authentication = Mockito.mock(Authentication.class);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(activityGenerator.ticketCreated(eq(user), any(Ticket.class))).thenReturn(activity);
    when(ticketMapper.mapToTicketResponse(any(Ticket.class))).thenReturn(ticketResponse);

    final TicketResponse result = ticketService.createTicket(request, authentication);

    // then
    verify(authentication, times(1)).getPrincipal();
    verify(activityGenerator, times(1)).ticketCreated(eq(user), any(Ticket.class));
    verify(activityService, times(1)).createActivity(activity);
    verify(ticketRepository).save(ticketCaptor.capture());
    verify(ticketMapper, times(1)).mapToTicketResponse(any(Ticket.class));

    final Ticket saved = ticketCaptor.getValue();
    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(saved.getTitle()).isEqualTo(request.title());
    Assertions.assertThat(saved.getDescription()).isEqualTo(request.description());
    Assertions.assertThat(saved.getPriority()).isEqualTo(request.priority());
    Assertions.assertThat(saved.getStatus()).isEqualTo(request.status());
    Assertions.assertThat(saved.getAssignedTo()).isNull();
  }

  @Test
  void shouldReturn_ticketResponse_onCreateTicket_withAssignee() {
    // mock
    final User user = User.builder()
      .id(101L)
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .role(Role.builder().role("User").authorities(
        List.of(Authority.builder().authority("ticket::create").build(), Authority.builder().authority("ticket::assign").build())
      ).build())
      .build();

    final User assignee = User.builder()
      .id(102L)
      .firstname("Louis")
      .email("louis@test.in")
      .password("Louis@01")
      .role(Role.builder().role("User").authorities(List.of(Authority.builder().authority("ticket::create").build())).build())
      .build();
    final Activity activity = Mockito.mock(Activity.class);
    final TicketResponse ticketResponse = Mockito.mock(TicketResponse.class);
  
    // given
    final TicketRequest request = new TicketRequest("Test 1", "Test ticket", Priority.LOW, Status.OPEN, "Louis", null);
    final Authentication authentication = Mockito.mock(Authentication.class);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(userRepository.findByFirstname(request.assignedTo())).thenReturn(Optional.of(assignee));
    when(activityGenerator.ticketCreated(eq(user), any(Ticket.class))).thenReturn(activity);
    when(ticketMapper.mapToTicketResponse(any(Ticket.class))).thenReturn(ticketResponse);

    final TicketResponse result = ticketService.createTicket(request, authentication);

    // then
    verify(authentication, times(1)).getPrincipal();
    verify(activityGenerator, times(1)).ticketCreated(eq(user), any(Ticket.class));
    verify(activityService, times(1)).createActivity(activity);
    verify(ticketRepository).save(ticketCaptor.capture());
    verify(notificationService, times(1)).sendNotification(eq(assignee), anyString());
    verify(ticketMapper, times(1)).mapToTicketResponse(any(Ticket.class));

    final Ticket saved = ticketCaptor.getValue();
    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(saved.getTitle()).isEqualTo(request.title());
    Assertions.assertThat(saved.getDescription()).isEqualTo(request.description());
    Assertions.assertThat(saved.getPriority()).isEqualTo(request.priority());
    Assertions.assertThat(saved.getStatus()).isEqualTo(request.status());
    Assertions.assertThat(saved.getAssignedTo()).isEqualTo(assignee);
  }

  @Test
  void shouldThrow_exception_onCreateTicket_ifAssigneeNotPresent() {
    // mock
    final User user = User.builder()
      .id(101L)
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .role(Role.builder().role("User").authorities(
        List.of(Authority.builder().authority("ticket::create").build(), Authority.builder().authority("ticket::assign").build())
      ).build())
      .build();
  
    // given
    final TicketRequest request = new TicketRequest("Test 1", "Test ticket", Priority.LOW, Status.OPEN, "Louis", null);
    final Authentication authentication = Mockito.mock(Authentication.class);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(userRepository.findByFirstname(request.assignedTo())).thenReturn(Optional.empty());

    // then
    Assertions.assertThatThrownBy(() -> ticketService.createTicket(request, authentication))
      .isInstanceOf(EntityNotFoundException.class);
  }

   @Test
  void shouldThrow_exception_onCreateTicket_whileAssigning_IfUserNotHaveAuthority() {
    // ticket::assign

    // mock
    final User user = User.builder()
      .id(101L)
      .firstname("Peter")
      .email("peter@test.in")
      .password("Peter@01")
      .role(Role.builder().role("User").authorities(List.of(Authority.builder().authority("ticket::create").build())).build())
      .build();
  
    // given
    final TicketRequest request = new TicketRequest("Test 1", "Test ticket", Priority.LOW, Status.OPEN, "Louis", null);
    final Authentication authentication = Mockito.mock(Authentication.class);

    // when
    when(authentication.getPrincipal()).thenReturn(user);

    // then
    Assertions.assertThatThrownBy(() -> ticketService.createTicket(request, authentication))
      .isInstanceOf(OperationNotPermittedException.class);
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldReturn_pagedEntity_ofAllTicket() {
    // mock
    final Page<Ticket> page = Mockito.mock(Page.class);
    final List<Ticket> tickets = List.of(Mockito.mock(Ticket.class));

    // given
    final int number = 0;
    final int size = 10;

    // when
    when(ticketRepository.findAll(any(Pageable.class))).thenReturn(page);
    when(page.getContent()).thenReturn(tickets);

    final PagedEntity<TicketResponse> result = ticketService.getTickets(number, size);

    // then
    verify(ticketRepository, times(1)).findAll(any(Pageable.class));
    verify(page, times(1)).hasNext();
    verify(page, times(1)).hasPrevious();
    verify(page, times(1)).getContent();
    verify(ticketMapper, times(1)).mapToTicketResponse(any(Ticket.class));

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldReturn_pagedEntity_ofAllTicket_ofAuthenticatedUser() {
    // mock
    final Page<Ticket> page = Mockito.mock(Page.class);
    final List<Ticket> tickets = List.of(Mockito.mock(Ticket.class));

    // given
    final Status status = Status.CLOSE;
    final int number = 0;
    final int size = 10;

    // wheni
    when(ticketRepository.findByStatus(eq(status), any(Pageable.class))).thenReturn(page);
    when(page.getContent()).thenReturn(tickets);

    final PagedEntity<TicketResponse> result = ticketService.getTicketByStatus(status, number, size);

    // then
    verify(ticketRepository, times(1)).findByStatus(eq(status), any(Pageable.class));
    verify(page, times(1)).hasNext();
    verify(page, times(1)).hasPrevious();
    verify(page, times(1)).getContent();
    verify(ticketMapper, times(1)).mapToTicketResponse(any(Ticket.class));

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  void shouldReturn_ticketResponse_forValidId() {
    // mock
    final Ticket ticket = Mockito.mock(Ticket.class);
    final TicketResponse response = Mockito.mock(TicketResponse.class);

    // given
    final Long id = 101L;

    // wheni
    when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
    when(ticketMapper.mapToTicketResponse(ticket)).thenReturn(response);

    final TicketResponse result = ticketService.getTicket(id);

    // then
    verify(ticketRepository, times(1)).findById(id);
    verify(ticketMapper, times(1)).mapToTicketResponse(any(Ticket.class));

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  void shouldThrow_exception_forInalidId_onGetTicket() {
    // given
    final Long id = 102L;

    // wheni
    when(ticketRepository.findById(id)).thenReturn(Optional.empty());

    // then
    Assertions.assertThatThrownBy(() -> ticketService.getTicket(id));
  }

  @Test
  void shouldReturn_map_ofTicketInfo() {
    // mock
    final List<Ticket> tickets = List.of(
      Ticket.builder().title("T1").description("test 1").priority(Priority.LOW).status(Status.OPEN).build(),
      Ticket.builder().title("T1").description("test 1").priority(Priority.LOW).status(Status.CLOSE).build()
    );
    // given
    final Authentication authentication = Mockito.mock(Authentication.class);

    // when
    when(ticketRepository.count()).thenReturn((long)tickets.size());
    when(ticketRepository.findAll()).thenReturn(tickets);
    
    final Map<String, Long> result = ticketService.getTicketsInfo(authentication);

    // then
    verify(ticketRepository, times(1)).findAll();
    verify(ticketRepository, times(1)).count();

    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result.get("open_tickets")).isEqualTo(1);
    Assertions.assertThat(result.get("closed_tickets")).isEqualTo(1);
    Assertions.assertThat(result.get("total_tickets")).isEqualTo(2);
  }

  @Test
  void shouldReturn_ticketResponse_whenTitleUpdated() {
    // mock
    final User user = Mockito.mock(User.class);
    final Ticket ticket = Ticket.builder()
      .title("Title")
      .description("description")
      .lock(false)
      .createdBy(user)
      .priority(Priority.LOW)
      .status(Status.OPEN)
      .build();
    final Activity activity = Mockito.mock(Activity.class);
    final TicketResponse ticketResponse = Mockito.mock(TicketResponse.class);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final Long id = 101L;
    final TicketPatchRequest request = new TicketPatchRequest("New Title", null, null, null, null, null);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
    when(activityGenerator.titleChanged(eq(user), eq(ticket), eq("Title"), eq("New Title"))).thenReturn(activity);

    when(user.getEmail()).thenReturn("user@test.in");

    when(ticketMapper.mapToTicketResponse(ticket)).thenReturn(ticketResponse);

    final TicketResponse result = ticketService.editTicket(id, request, authentication);

    // then
    verify(authentication, times(1)).getPrincipal();
    verify(ticketRepository, times(1)).findById(id);
    verify(activityGenerator, times(1)).titleChanged(eq(user), eq(ticket), eq("Title"), eq("New Title"));
    verify(ticketRepository, times(1)).save(ticketCaptor.capture());
    verify(activityService, times(1)).saveActivities(anyList());
    verify(ticketMapper, times(1)).mapToTicketResponse(ticket);

    final Ticket updated = ticketCaptor.getValue();

    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(updated.getTitle()).isEqualTo(request.title());
  }

  @Test
  void shouldReturn_ticketResponse_whenDescriptionUpdated() {
    // mock
    final User user = Mockito.mock(User.class);
    final Ticket ticket = Ticket.builder()
      .title("Title")
      .description("description")
      .priority(Priority.LOW)
      .status(Status.OPEN)
      .createdBy(user)
      .lock(false)
      .build();
    final Activity activity = Mockito.mock(Activity.class);
    final TicketResponse ticketResponse = Mockito.mock(TicketResponse.class);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final Long id = 101L;
    final TicketPatchRequest request = new TicketPatchRequest(null, "New description", null, null, null, null);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
    when(activityGenerator.descriptionChanged(user, ticket)).thenReturn(activity);
    when(ticketMapper.mapToTicketResponse(ticket)).thenReturn(ticketResponse);
    when(user.getEmail()).thenReturn("user@test.in");

    final TicketResponse result = ticketService.editTicket(id, request, authentication);

    // then
    verify(authentication, times(1)).getPrincipal();
    verify(ticketRepository, times(1)).findById(id);
    verify(activityGenerator, times(1)).descriptionChanged(user, ticket);
    verify(ticketRepository, times(1)).save(ticketCaptor.capture());
    verify(activityService, times(1)).saveActivities(anyList());
    verify(ticketMapper, times(1)).mapToTicketResponse(ticket);

    final Ticket updated = ticketCaptor.getValue();

    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(updated.getDescription()).isEqualTo(request.description());
  }

  @Test
  void shouldReturn_ticketResponse_whenAssigneeToUpdated_hasPermission() {
    // ticket::assign
    // mock
    final User user = Mockito.mock(User.class);
    final User assignedTo = Mockito.mock(User.class);
    final Ticket ticket = Ticket.builder()
      .id(101l)
      .title("Title")
      .description("description")
      .priority(Priority.LOW)
      .createdBy(user)
      .status(Status.OPEN)
      .lock(false)
      .build();
    final Activity activity = Mockito.mock(Activity.class);
    final TicketResponse ticketResponse = Mockito.mock(TicketResponse.class);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final Long id = 101L;
    final TicketPatchRequest request = new TicketPatchRequest(null, null, "Louis", null, null, null);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
    when(activityGenerator.assignedToChanged(eq(user), eq(ticket), eq(""), eq(request.assignedTo()))).thenReturn(activity);
    when(userRepository.findByFirstname(request.assignedTo())).thenReturn(Optional.of(assignedTo));
    when(user.getFirstname()).thenReturn("Joma");
    when(user.getUsername()).thenReturn("joma@test.in");
    when(ticketMapper.mapToTicketResponse(ticket)).thenReturn(ticketResponse);
    when(assignedTo.getUsername()).thenReturn("louis@dev.in");
    when(user.hasAuthority(IAuthority.ASSIGN_TICKET)).thenReturn(true);

    final TicketResponse result = ticketService.editTicket(id, request, authentication);

    // then
    verify(authentication, times(1)).getPrincipal();
    verify(ticketRepository, times(1)).findById(id);
    verify(activityGenerator, times(1)).assignedToChanged(eq(user), eq(ticket), eq(""), eq(request.assignedTo()));
    verify(ticketRepository, times(1)).save(ticketCaptor.capture());
    verify(activityService, times(1)).saveActivities(anyList());
    verify(notificationService, times(1)).sendNotification(eq(assignedTo), eq("Joma assignee a #101 ticket to you"));
    verify(ticketMapper, times(1)).mapToTicketResponse(ticket);

    final Ticket updated = ticketCaptor.getValue();

    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(updated.getAssignedTo()).isEqualTo(assignedTo);
  }

  @Test
  void shouldThrow_exception_whenAssigneeToUpdated_hasNoPermission() {
    // mock
    final User user = Mockito.mock(User.class);
    final Ticket ticket = Ticket.builder()
      .title("Title")
      .description("description")
      .priority(Priority.LOW)
      .createdBy(user)
      .status(Status.OPEN)
      .lock(false)
      .build();

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final Long id = 101L;
    final TicketPatchRequest request = new TicketPatchRequest(null, null, "Louis", null, null, null);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
    when(user.hasAuthority(IAuthority.ASSIGN_TICKET)).thenReturn(false);

    Assertions.assertThatThrownBy(() -> ticketService.editTicket(id, request, authentication))
      .isInstanceOf(OperationNotPermittedException.class);
  }

  @Test
  void shouldThrow_exception_whenAssigneeToUpdated_andAssigneeNotPresent() {
    // ticket::assign
    // mock
    final User user = Mockito.mock(User.class);
    final Ticket ticket = Ticket.builder()
      .title("Title")
      .description("description")
      .priority(Priority.LOW)
      .status(Status.OPEN)
      .createdBy(user)
      .lock(false)
      .build();
    final Activity activity = Mockito.mock(Activity.class);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final Long id = 101L;
    final TicketPatchRequest request = new TicketPatchRequest(null, null, "Louis", null, null, null);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
    when(activityGenerator.assignedToChanged(eq(user), eq(ticket), eq(""), eq(request.assignedTo()))).thenReturn(activity);
    when(userRepository.findByFirstname(request.assignedTo())).thenReturn(Optional.empty());
    when(user.hasAuthority(IAuthority.ASSIGN_TICKET)).thenReturn(true);

    // then
    Assertions.assertThatThrownBy(() -> ticketService.editTicket(id, request, authentication))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldReturn_ticketResponse_whenPriorityUpdated() {
    // mock
    final User user = Mockito.mock(User.class);
    final Ticket ticket = Ticket.builder()
      .title("Title")
      .description("description")
      .priority(Priority.LOW)
      .status(Status.OPEN)
      .createdBy(user)
      .lock(false)
      .build();
    final Activity activity = Mockito.mock(Activity.class);
    final TicketResponse ticketResponse = Mockito.mock(TicketResponse.class);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final Long id = 101L;
    final TicketPatchRequest request = new TicketPatchRequest(null, null, null, Priority.MEDIUM, null, null);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
    when(activityGenerator.priorityChanged(user, ticket, ticket.getPriority(), request.priority())).thenReturn(activity);
    when(ticketMapper.mapToTicketResponse(ticket)).thenReturn(ticketResponse);
    when(user.getEmail()).thenReturn("user@test.in");

    final TicketResponse result = ticketService.editTicket(id, request, authentication);

    // then
    verify(authentication, times(1)).getPrincipal();
    verify(ticketRepository, times(1)).findById(id);
    verify(activityGenerator, times(1)).priorityChanged(user, ticket, Priority.LOW, Priority.MEDIUM);
    verify(ticketRepository, times(1)).save(ticketCaptor.capture());
    verify(activityService, times(1)).saveActivities(anyList());
    verify(ticketMapper, times(1)).mapToTicketResponse(ticket);

    final Ticket updated = ticketCaptor.getValue();

    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(updated.getPriority()).isEqualTo(request.priority());
  }

  @Test
  void shouldReturn_ticketResponse_whenStatusUpdated() {
    // mock
    final User user = Mockito.mock(User.class);
    final Ticket ticket = Ticket.builder()
      .title("Title")
      .description("description")
      .priority(Priority.LOW)
      .status(Status.OPEN)
      .createdBy(user)
      .lock(false)
      .build();
    final Activity activity = Mockito.mock(Activity.class);
    final TicketResponse ticketResponse = Mockito.mock(TicketResponse.class);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final Long id = 101L;
    final TicketPatchRequest request = new TicketPatchRequest(null, null, null, null, Status.CLOSE, null);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
    when(activityGenerator.statusChanged(user, ticket, ticket.getStatus(), request.status())).thenReturn(activity);
    when(ticketMapper.mapToTicketResponse(ticket)).thenReturn(ticketResponse);
    when(user.getEmail()).thenReturn("user@test.in");

    final TicketResponse result = ticketService.editTicket(id, request, authentication);

    // then
    verify(authentication, times(1)).getPrincipal();
    verify(ticketRepository, times(1)).findById(id);
    verify(activityGenerator, times(1)).statusChanged(user, ticket, Status.OPEN, Status.CLOSE);
    verify(ticketRepository, times(1)).save(ticketCaptor.capture());
    verify(activityService, times(1)).saveActivities(anyList());
    verify(ticketMapper, times(1)).mapToTicketResponse(ticket);

    final Ticket updated = ticketCaptor.getValue();

    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(updated.getStatus()).isEqualTo(request.status());
  }

  @Test
  void shouldReturn_ticketResponse_whenUpdated_hasProperAuthority() {
    // ticket::edit
    // mock
    final User user = Mockito.mock(User.class);
    final Ticket ticket = Ticket.builder()
      .title("Title")
      .description("description")
      .priority(Priority.LOW)
      .status(Status.OPEN)
      .createdBy(User.builder().email("notowner@test.in").build())
      .lock(false)
      .build();

    final Activity activity = Mockito.mock(Activity.class);
    final TicketResponse ticketResponse = Mockito.mock(TicketResponse.class);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final Long id = 101L;
    final TicketPatchRequest request = new TicketPatchRequest(null, null, null, null, Status.CLOSE, null);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
    when(activityGenerator.statusChanged(user, ticket, ticket.getStatus(), request.status())).thenReturn(activity);
    when(ticketMapper.mapToTicketResponse(ticket)).thenReturn(ticketResponse);
    when(user.getEmail()).thenReturn("user@test.in");
    when(user.hasAuthority(IAuthority.EDIT_TICKET)).thenReturn(true);

    final TicketResponse result = ticketService.editTicket(id, request, authentication);

    // then
    verify(authentication, times(1)).getPrincipal();
    verify(ticketRepository, times(1)).findById(id);
    verify(activityGenerator, times(1)).statusChanged(user, ticket, Status.OPEN, Status.CLOSE);
    verify(ticketRepository, times(1)).save(ticketCaptor.capture());
    verify(activityService, times(1)).saveActivities(anyList());
    verify(ticketMapper, times(1)).mapToTicketResponse(ticket);

    final Ticket updated = ticketCaptor.getValue();

    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(updated.getStatus()).isEqualTo(request.status());
  }

  @Test
  void shouldThrow_exception_whenUpdated_hasNoAuthority() {
    // mock
    final User user = Mockito.mock(User.class);
    final Ticket ticket = Ticket.builder()
      .title("Title")
      .description("description")
      .priority(Priority.LOW)
      .status(Status.OPEN)
      .createdBy(User.builder().email("notowner@test.in").build())
      .lock(false)
      .build();

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final Long id = 101L;
    final TicketPatchRequest request = new TicketPatchRequest(null, null, null, null, Status.CLOSE, null);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
    when(user.getEmail()).thenReturn("user@test.in");
    when(user.hasAuthority(IAuthority.EDIT_TICKET)).thenReturn(false);

    Assertions.assertThatThrownBy(() -> ticketService.editTicket(id, request, authentication))
      .isInstanceOf(OperationNotPermittedException.class);
  }

   @Test
  void shouldThrows_exception_whenTicketUpdated_IfTicketIsLocked() {
    // mock
    final User user = Mockito.mock(User.class);
    final Ticket ticket = Ticket.builder()
      .title("Title")
      .description("description")
      .priority(Priority.LOW)
      .status(Status.OPEN)
      .lock(true)
      .build();
    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final Long id = 101L;
    final TicketPatchRequest request = new TicketPatchRequest(null, null, null, null, Status.CLOSE, null);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));

    Assertions.assertThatThrownBy(() -> ticketService.editTicket(id, request, authentication))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldThrow_exception_whenUpdated_forInvalidId() {
    // mock
    final User user = Mockito.mock(User.class);

    // given
    final Authentication authentication = Mockito.mock(Authentication.class);
    final Long id = 102L;
    final TicketPatchRequest request = new TicketPatchRequest(null, null, null, Priority.MEDIUM, null, null);

    // when
    when(authentication.getPrincipal()).thenReturn(user);
    when(ticketRepository.findById(id)).thenReturn(Optional.empty());

    // then
    Assertions.assertThatThrownBy(() -> ticketService.editTicket(id, request, authentication))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldDelete_ticket_withNoAssignee_andValidId() {
    // mock
    final Ticket ticket = Mockito.mock(Ticket.class);
    final User user = Mockito.mock(User.class);
    final Authentication authentication = Mockito.mock(Authentication.class);

    // given
    final Long id = 101L;

    // when
    when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
    when(authentication.getPrincipal()).thenReturn(user);
    when(ticket.getCreatedBy()).thenReturn(user);
    when(user.getEmail()).thenReturn("user@test.in");

    ticketService.deleteTicket(id, authentication);

    // then
    verify(ticketRepository, times(1)).findById(id);
    verify(ticket, times(1)).getAssignedTo();
    verify(ticketRepository, times(1)).delete(ticket);
  }

  @Test
  void shouldDelete_ticket_withNoAssignee_andValidId_andProperAuthority() {
    // ticket::delete
    // mock
    final Ticket ticket = Mockito.mock(Ticket.class);
    final User user = Mockito.mock(User.class);
    final Authentication authentication = Mockito.mock(Authentication.class);

    // given
    final Long id = 101L;

    // when
    when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
    when(authentication.getPrincipal()).thenReturn(user);
    when(ticket.getCreatedBy()).thenReturn(User.builder().email("noowner@test.in").build());
    when(user.getEmail()).thenReturn("user@test.in");
    when(user.hasAuthority(IAuthority.DELETE_TICKET)).thenReturn(true);

    ticketService.deleteTicket(id, authentication);

    // then
    verify(ticketRepository, times(1)).findById(id);
    verify(ticket, times(1)).getAssignedTo();
    verify(ticketRepository, times(1)).delete(ticket);
  }

  @Test
  void shouldDelete_ticket_withNoAssignee_andValidId_andNoProperAuthority_orNotAOwner() {
    // mock
    final Ticket ticket = Mockito.mock(Ticket.class);
    final User user = Mockito.mock(User.class);
    final Authentication authentication = Mockito.mock(Authentication.class);

    // given
    final Long id = 101L;

    // when
    when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
    when(authentication.getPrincipal()).thenReturn(user);
    when(ticket.getCreatedBy()).thenReturn(User.builder().email("noowner@test.in").build());
    when(user.getEmail()).thenReturn("user@test.in");
    when(user.hasAuthority(IAuthority.DELETE_TICKET)).thenReturn(false);

    Assertions.assertThatThrownBy(() -> ticketService.deleteTicket(id, authentication))
      .isInstanceOf(OperationNotPermittedException.class);
  }

  @Test
  void shouldDelete_ticket_withAssignee_andValidId() {
    // mock
    final Ticket ticket = Mockito.mock(Ticket.class);
    final User user = Mockito.mock(User.class);
    final Authentication authentication = Mockito.mock(Authentication.class);

    // given
    final Long id = 101L;

    // when
    when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
    when(authentication.getPrincipal()).thenReturn(user);
    when(ticket.getCreatedBy()).thenReturn(user);
    when(ticket.getAssignedTo()).thenReturn(user);
    when(user.getEmail()).thenReturn("user@test.in");

    ticketService.deleteTicket(id, authentication);

    // then
    verify(ticketRepository, times(1)).findById(id);
    verify(ticket, times(1)).getAssignedTo();
    verify(ticketRepository, times(1)).save(ticket);
    verify(ticketRepository, times(1)).delete(ticket);
  }

  @Test
  void shouldThrow_exception_forInvalidId() {
    // given
    final Long id = 102L;
    final Authentication authentication = Mockito.mock(Authentication.class);

    // when
    when(ticketRepository.findById(id)).thenReturn(Optional.empty());

    // then
    Assertions.assertThatThrownBy(() -> ticketService.deleteTicket(id, authentication))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldThrow_exception_forLockedTicket() {
    final Ticket ticket = Mockito.mock(Ticket.class);
    // given
    final Long id = 101L;
    final Authentication authentication = Mockito.mock(Authentication.class);

    // when
    when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
    when(ticket.getLock()).thenReturn(true);

    // then
    Assertions.assertThatThrownBy(() -> ticketService.deleteTicket(id, authentication))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldLockTicket_andReturnTicketResponse_forValidId() {
    final Ticket ticket = Mockito.mock(Ticket.class);
    final TicketResponse ticketResponse = Mockito.mock(TicketResponse.class);

    final Long id = 101L;

    when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
    when(ticketMapper.mapToTicketResponse(ticket)).thenReturn(ticketResponse);

    final TicketResponse result = ticketService.lockTicket(101L);

    verify(ticketRepository, times(1)).findById(id);
    verify(ticketMapper, times(1)).mapToTicketResponse(ticket);
    Assertions.assertThat(result).isNotNull();
  }

  @Test
  void shouldThrow_exceptionOnLockTicket_forInvalidId() {
    final Long id = 101L;

    when(ticketRepository.findById(id)).thenReturn(Optional.empty());

    Assertions.assertThatThrownBy(() -> ticketService.lockTicket(101L))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Ticket not found");
  }

  @Test
  void shouldUnlockTicket_andReturnTicketResponse_forValidId() {
    final Ticket ticket = Mockito.mock(Ticket.class);
    final TicketResponse ticketResponse = Mockito.mock(TicketResponse.class);

    final Long id = 101L;

    when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
    when(ticketMapper.mapToTicketResponse(ticket)).thenReturn(ticketResponse);

    final TicketResponse result = ticketService.unlockTicket(101L);

    verify(ticketRepository, times(1)).findById(id);
    verify(ticketMapper, times(1)).mapToTicketResponse(ticket);
    Assertions.assertThat(result).isNotNull();
  }

  @Test
  void shouldThrow_exceptionOnUnlockTicket_forInvalidId() {
    final Long id = 101L;

    when(ticketRepository.findById(id)).thenReturn(Optional.empty());

    Assertions.assertThatThrownBy(() -> ticketService.unlockTicket(101L))
      .isInstanceOf(EntityNotFoundException.class);
  }
}
