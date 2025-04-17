package com.example.latte_api.ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.latte_api.activity.Activity;
import com.example.latte_api.activity.ActivityService;
import com.example.latte_api.activity.utils.ActivityGenerator;
import com.example.latte_api.error.OperationNotPermittedException;
import com.example.latte_api.role.authority.IAuthority;
import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.ticket.dto.TicketPatchRequest;
import com.example.latte_api.ticket.dto.TicketRequest;
import com.example.latte_api.ticket.dto.TicketResponse;
import com.example.latte_api.ticket.enums.Status;
import com.example.latte_api.ticket.mapper.TicketMapper;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {
  private final TicketRepository ticketRepository;
  private final UserRepository userRepository;

  private final TicketMapper ticketMapper;

  private final ActivityGenerator activityGenerator;
  private final ActivityService activityService;

  @Transactional
  public TicketResponse createTicket(TicketRequest request, Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    User assignTo = null;

    if (request.assignedTo() != null && !request.assignedTo().isEmpty()) {
      if(!user.hasAuthority(IAuthority.ASSIGN_TICKET)) {
        throw new OperationNotPermittedException();
      }

      assignTo = userRepository.findByFirstname(request.assignedTo()).orElseThrow(
        () -> new EntityNotFoundException("Assignee user not found")
      );
    }

    Ticket ticket = Ticket.builder()
      .title(request.title())
      .description(request.description())
      .priority(request.priority())
      .status(request.status())
      .lock(false)
      .createdBy(user)
      .build();

    if (assignTo != null) {
      ticket.setAssignedTo(assignTo);
    }
    ticketRepository.save(ticket);
    activityService.createActivity(activityGenerator.ticketCreated(user, ticket));
    return ticketMapper.mapToTicketResponse(ticket);
  }

  public PagedEntity<TicketResponse> getTickets(int number, int size) {
    Pageable pageable = PageRequest.of(number, size, Sort.by(Direction.DESC, "createdAt"));
    Page<Ticket> page = ticketRepository.findAll(pageable);

    PagedEntity<TicketResponse> response = new PagedEntity<>();
    response.setNext(page.hasNext());
    response.setPrev(page.hasPrevious());
    response.setContent(
      page.getContent()
      .stream()
      .map(t -> ticketMapper.mapToTicketResponse(t))
      .toList()
    );

    return response;
  }

  public PagedEntity<TicketResponse> getTicketByStatus(Status status, int number, int size) {
    Pageable pageable = PageRequest.of(number, size, Sort.by(Direction.DESC, "createdAt"));
    Page<Ticket> page = ticketRepository.findByStatus(status, pageable);

    PagedEntity<TicketResponse> response = new PagedEntity<>();
    response.setNext(page.hasNext());
    response.setPrev(page.hasPrevious());
    response.setContent(
      page.getContent()
      .stream()
      .map(t -> ticketMapper.mapToTicketResponse(t))
      .toList()
    );

    return response;
  }

  public Map<String, Integer> getTicketsInfo(Authentication authentication) {
    List<Ticket> tickets = ticketRepository.findAll();
    int closed = tickets.stream().filter(t -> t.getStatus().equals(Status.CLOSE)).toList().size();
    int open = tickets.stream().filter(t -> t.getStatus().equals(Status.OPEN)).toList().size();
    return Map.of("open_tickets", open, "closed_tickets", closed);
  }

  public TicketResponse getTicket(Long id) {
    Ticket ticket = ticketRepository.findById(id).orElseThrow(
      () -> new EntityNotFoundException("Ticket not found")
    );
    return ticketMapper.mapToTicketResponse(ticket);
  }

  @Transactional
  public TicketResponse editTicket(Long id, TicketPatchRequest request, Authentication authentication) {
    User user = (User) authentication.getPrincipal();

    Ticket ticket = ticketRepository.findById(id).orElseThrow(
      () -> new EntityNotFoundException("Ticket not found")
    );

    if (ticket.getLock()) {
      throw new IllegalStateException("Ticket is locked");
    }

    List<Activity> activities = new ArrayList<>();

    if (request.title() != null && !request.title().equals(ticket.getTitle())) {
      if (!canEditTicket(user, ticket)) {throw new OperationNotPermittedException();}
      activities.add(activityGenerator.titleChanged(user, ticket, ticket.getTitle(), request.title()));
      ticket.setTitle(request.title());
    }

    if (request.description() != null && !request.description().equals(ticket.getDescription())) {
      if (!canEditTicket(user, ticket)) {throw new OperationNotPermittedException();}
      activities.add(activityGenerator.descriptionChanged(user, ticket));
      ticket.setDescription(request.description());
    }

    if (request.priority() != null && !request.priority().equals(ticket.getPriority())) {
      if (!canEditTicket(user, ticket)) {throw new OperationNotPermittedException();}
      activities.add(activityGenerator.priorityChanged(user, ticket, ticket.getPriority(), request.priority()));
      ticket.setPriority(request.priority());
    }

    if (request.status() != null && !request.status().equals(ticket.getStatus())) {
      if (!canEditTicket(user, ticket)) {throw new OperationNotPermittedException();}
      activities.add(activityGenerator.statusChanged(user, ticket, ticket.getStatus(), request.status()));
      ticket.setStatus(request.status());
    }

    if (request.assignedTo() != null) {
      if (!user.hasAuthority(IAuthority.ASSIGN_TICKET)) {throw new OperationNotPermittedException();}
      assignTicket(ticket, request, user, activities);
    }

    ticketRepository.save(ticket);
    if (!activities.isEmpty()) {
      activityService.saveActivities(activities);
    }
    return ticketMapper.mapToTicketResponse(ticket);
  }

  private void assignTicket(Ticket ticket, TicketPatchRequest request, User user, List<Activity> activities) {
    String old = ticket.getAssignedTo() == null? "" : ticket.getAssignedTo().getFirstname();
    String curr = request.assignedTo();

    if (!old.equals(curr)) {
      activities.add(activityGenerator.assignedToChanged(user, ticket, old, curr));

      User assignedTo = null;
      if (!request.assignedTo().isEmpty()) {
        assignedTo = userRepository.findByFirstname(request.assignedTo()).orElseThrow(
          () -> new EntityNotFoundException("User not found")
        );
      }
      ticket.setAssignedTo(assignedTo);
    }
  }

  private boolean canEditTicket(User user, Ticket ticket) {
    return isOwner(ticket, user) || user.hasAuthority(IAuthority.EDIT_TICKET);
  }

  @Transactional
  public TicketResponse lockTicket(Long id) {
    Ticket ticket = ticketRepository.findById(id).orElseThrow(
      () -> new EntityNotFoundException("Ticket not found")
    );
    ticket.setLock(true);
    ticketRepository.save(ticket);
    return ticketMapper.mapToTicketResponse(ticket);
  }

  @Transactional
  public TicketResponse unlockTicket(Long id) {
    Ticket ticket = ticketRepository.findById(id).orElseThrow(
      () -> new EntityNotFoundException("Ticket not found")
    );
    ticket.setLock(false);
    ticketRepository.save(ticket);
    return ticketMapper.mapToTicketResponse(ticket);
  }

  public void deleteTicket(Long id, Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    Ticket ticket = ticketRepository.findById(id).orElseThrow(
      () -> new EntityNotFoundException("Ticket not found")
    );

    if (ticket.getLock()) {
      throw new IllegalStateException("Ticket is locked");
    }

    if (!isOwner(ticket, user) && !user.hasAuthority(IAuthority.DELETE_TICKET)) {
      System.out.println("No permit");
      throw new OperationNotPermittedException();
    }
     
    if(ticket.getAssignedTo() != null) {
      ticket.setAssignedTo(null);
      ticketRepository.save(ticket);
    }
    ticketRepository.delete(ticket);
  }

  private boolean isOwner(Ticket ticket, User user) {
    return ticket.getCreatedBy().getEmail().equals(user.getEmail());
  }
}
