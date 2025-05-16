package com.example.latte_api.user;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.latte_api.activity.Activity;
import com.example.latte_api.activity.ActivityRepository;
import com.example.latte_api.role.Role;
import com.example.latte_api.role.RoleRepository;
import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.ticket.Ticket;
import com.example.latte_api.ticket.TicketRepository;
import com.example.latte_api.user.dto.UserRequest;
import com.example.latte_api.user.dto.UserResponse;
import com.example.latte_api.user.mapper.UserMapper;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final TicketRepository ticketRepository;
  private final ActivityRepository activityRepository;

  private final UserMapper userMapper;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByEmail(username).orElseThrow(
      () -> new UsernameNotFoundException(String.format("User with username:`%s` not found", username))
    );
  }

  public Map<String, Long> getUserCount() {
    long count = userRepository.count();
    return Map.of("user_count", count);
  }

  public PagedEntity<UserResponse> getUsers(int number, int size) {
    Pageable pageable = PageRequest.of(number, size, Sort.by(Direction.DESC, "createdAt"));
    Page<User> page = userRepository.findAll(pageable);

    PagedEntity<UserResponse> response = new PagedEntity<>();
    response.setNext(page.hasNext());
    response.setPrev(page.hasPrevious());
    response.setContent(page.getContent().stream().map(u -> userMapper.mapToUserDto(u)).toList());
    return response;
  }

  public PagedEntity<String> getUserList(int number, int size) {
    Pageable pageable = PageRequest.of(number, size);
    Page<User> page = userRepository.findAll(pageable);

    PagedEntity<String> response = new PagedEntity<>();
    response.setNext(page.hasNext());
    response.setPrev(page.hasPrevious());
    response.setContent(page.getContent().stream().map(u -> u.getFirstname()).toList());
    return response;
  }

  public UserResponse getUser(Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    return userMapper.mapToUserDto(user);
  }

  public UserResponse getUser(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(
      () -> new EntityNotFoundException("User not found")
    );
    return userMapper.mapToUserDto(user);
  }

  @Transactional
  public UserResponse updateUser(UserRequest request, Authentication authentication) {
    User user = (User) authentication.getPrincipal();

    if (!user.isEditable()) {
      throw new IllegalStateException("User cannot be edited");
    }

    user.setEmail(request.email());
    user.setFirstname(request.firstname());
    userRepository.save(user);

    return userMapper.mapToUserDto(user);
  }

  @Transactional
  public UserResponse updateUser(UserRequest request, String _user) {
    User user = userRepository.findByEmail(_user).orElseThrow(
      () -> new EntityNotFoundException("User not found")
    );

    if (!user.isEditable()) {
      throw new IllegalStateException("User cannot be edited");
    }

    user.setEmail(request.email());
    user.setFirstname(request.firstname());

    if (!request.role().equals(user.getRole().getRole())) {
      Role role = roleRepository.findByRole(request.role()).orElseThrow(
        () -> new EntityNotFoundException("Role not found")
      );
      user.setRole(role);
    }
    
    userRepository.save(user);
    return userMapper.mapToUserDto(user);
  }

  @Transactional
  public void deleteUser(String _user) {
    User user = userRepository.findByEmail(_user).orElseThrow(
      () -> new EntityNotFoundException("User not found")
    );

    if (!user.isDeletable()) {
      throw new IllegalStateException("User cannot be deleted");
    }

    User admin = userRepository.findByDeletable(false).get(0);

    List<Ticket> createdTickets = ticketRepository.findByCreatedBy(user);
    for (Ticket ticket : createdTickets) {
      ticket.setCreatedBy(admin);
    }
    ticketRepository.saveAll(createdTickets);

    List<Activity> activities = activityRepository.findByAuthor(user);
    for (Activity activity: activities) {
      activity.setAuthor(admin);
    }
    activityRepository.saveAll(activities);

    List<Ticket> assignedTickets = ticketRepository.findByAssignedTo(user);
    for (Ticket ticket : assignedTickets) {
      ticket.setAssignedTo(null);
    }
    
    ticketRepository.saveAll(assignedTickets);
    userRepository.delete(user);
  }
}
