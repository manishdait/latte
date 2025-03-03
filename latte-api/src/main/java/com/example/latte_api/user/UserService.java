package com.example.latte_api.user;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.user.dto.UserDto;
import com.example.latte_api.user.mapper.UserMapper;
import com.example.latte_api.user.role.Role;
import com.example.latte_api.user.role.RoleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  private final UserMapper userMapper;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByEmail(username).orElseThrow(
      () -> new UsernameNotFoundException(String.format("User with username:`%s` not found", username))
    );
  }

  public PagedEntity<UserDto> getUsers(int number, int size) {
    Pageable pageable = PageRequest.of(number, size);
    Page<User> page = userRepository.findAll(pageable);

    PagedEntity<UserDto> response = new PagedEntity<>();
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

  public UserDto getUser(Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    return userMapper.mapToUserDto(user);
  }

  public UserDto getUser(String email) {
    User user = userRepository.findByEmail(email).orElseThrow();
    return userMapper.mapToUserDto(user);
  }

  @Transactional
  public UserDto updateUser(UserDto request, Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    user.setEmail(request.email());
    user.setFirstname(request.firstname());
    userRepository.save(user);

    return userMapper.mapToUserDto(user);
  }

  @Transactional
  public UserDto updateUser(UserDto request, String _user) {
    User user = userRepository.findByEmail(_user).orElseThrow();

    user.setEmail(request.email());
    user.setFirstname(request.firstname());

    if (!request.role().equals(user.getRole().getRole())) {
      Role role = roleRepository.findByRole(request.role()).orElseThrow();
      user.setRole(role);
    }
    
    userRepository.save(user);
    return userMapper.mapToUserDto(user);
  }

  public void deleteUser(String _user) {
    User user = userRepository.findByEmail(_user).orElseThrow();
    userRepository.delete(user);
  }

  public Map<String, Long> getUserCount() {
    long count = userRepository.count();
    return Map.of("user_count", count);
  }
}
