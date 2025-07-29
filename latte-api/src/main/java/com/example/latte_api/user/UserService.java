package com.example.latte_api.user;

import java.util.List;

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
import lombok.extern.slf4j.Slf4j;

/*
 * User Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final TicketRepository ticketRepository;
  private final ActivityRepository activityRepository;

  private final UserMapper userMapper;

  /**
   * Loads user details by username (email).
   *
   * @param username The email address of the user.
   * @return UserDetails object if found.
   * @throws UsernameNotFoundException if the user is not found.
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.info("Attempting to load user by username: {}", username);

    return userRepository.findByEmail(username).orElseThrow(() -> {
      log.warn("User with username: {} not found during authentication.", username);
      throw new UsernameNotFoundException(String.format("User with username:`%s` not found", username));
    });
  }

  /**
   * Retrieves a paginated list of all users with detailed information.
   * 
   * @param pageNumber The page number (0-indexed).
   * @param pageSize The number of items per page.
   * @return A PagedEntity containing UserResponse.
   */
  public PagedEntity<UserResponse> getUsers(int pageNumber, int pageSize) {
    Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Direction.DESC, "createdAt"));
    Page<User> page = userRepository.findAll(pageable);

    PagedEntity<UserResponse> response = new PagedEntity<>();
    response.setNext(page.hasNext());
    response.setPrevious(page.hasPrevious());
    response.setTotalElement(page.getTotalElements());
    response.setContent(page.getContent().stream().map(u -> userMapper.mapToUserDto(u)).toList());

    return response;
  }

  /**
   * Retrieves a paginated list of only user first names.
   *
   * @param pageNumber The page number.
   * @param pageSize The number of items per page.
   * @return A PagedEntity containing user first names as Strings.
   */
  public PagedEntity<String> getPagedUserFirstnames(int pageNumber, int pageSize) {
    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    Page<User> page = userRepository.findAll(pageable);

    PagedEntity<String> response = new PagedEntity<>();
    response.setNext(page.hasNext());
    response.setPrevious(page.hasPrevious());
    response.setTotalElement(page.getTotalElements());
    response.setContent(page.getContent().stream().map(u -> u.getFirstname()).toList());

    return response;
  }

  /**
   * Retrieves the details of the currently authenticated user.
   *
   * @param authentication The Spring Security Authentication object.
   * @return UserResponse DTO of the current user.
   */
  public UserResponse getCurrentUser(Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    return userMapper.mapToUserDto(user);
  }

  /**
   * Retrieves user details by their email address.
   *
   * @param email The email address of the user.
   * @return UserResponse DTO of the found user.
   * @throws EntityNotFoundException if the user is not found.
   */
  public UserResponse getUserByEmail(String userEmail) {
    User user = findUserByEmail(userEmail);
    return userMapper.mapToUserDto(user);
  }

  /**
   * Updates the details of the currently authenticated user.
   *
   * @param request The UserRequest DTO containing updated user information.
   * @param authentication The Spring Security Authentication object.
   * @return UserResponse DTO of the updated user.
   * @throws IllegalStateException if the user is not editable.
   */
  @Transactional
  public UserResponse updateCurrentUser(UserRequest request, Authentication authentication) {
    User user = (User) authentication.getPrincipal();

    if (!user.isEditable()) {
      throw new IllegalStateException(String.format("User '%s' cannot be edited.", user.getEmail()));
    }

    user.setEmail(request.email());
    user.setFirstname(request.firstname());
    userRepository.save(user);

    return userMapper.mapToUserDto(user);
  }

  /**
   * Updates the details of a specific user identified by their email address.
   *
   * @param request The UserRequest DTO containing updated user information.
   * @param userEmail The email address of the user to update.
   * @return UserResponse DTO of the updated user.
   * @throws EntityNotFoundException if the user or specified role is not found.
   * @throws IllegalStateException if the user is not editable.
   */
  @Transactional
  public UserResponse updateUserByEmail(UserRequest request, String userEmail) {
    User user = findUserByEmail(userEmail);

    if (!user.isEditable()) {
      throw new IllegalStateException(String.format("User '%s' cannot be edited.", userEmail));
    }

    user.setEmail(request.email());
    user.setFirstname(request.firstname());

    if (!request.role().equals(user.getRole().getRole())) {
      Role role = roleRepository.findByRole(request.role()).orElseThrow(
        () -> new EntityNotFoundException(String.format("Role '%s' not found.", request.role()))
      );
      user.setRole(role);
    }
    
    userRepository.save(user);
    return userMapper.mapToUserDto(user);
  }

  /**
   * Deletes a user by their email address, reassigning their created tickets and activities
   * to a admin user. Assigned tickets are unassigned.
   *
   * @param userEmail The email address of the user to delete.
   * @throws EntityNotFoundException if the user to delete or the admin user is not found.
   * @throws IllegalStateException if the user is not deletable.
   */
  @Transactional
  public void deleteUserByEmail(String userEmail) {
    User user = findUserByEmail(userEmail);

    if (!user.isDeletable()) {
      throw new IllegalStateException(String.format("User '%s' cannot be deleted.", userEmail));
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

  /**
   * Helper method to find a User by its email.
   * 
   * @param userEmail The email of the user to find.
   * @return The found User entity.
   * @throws EntityNotFoundException If the user is not found.
   */
  private User findUserByEmail(String userEmail) {
    return userRepository.findByEmail(userEmail).orElseThrow(
      () -> new EntityNotFoundException(String.format("User with email '%s' not found.", userEmail))
    );
  }
}
