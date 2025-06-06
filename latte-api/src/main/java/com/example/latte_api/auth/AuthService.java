package com.example.latte_api.auth;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.latte_api.auth.dto.AuthRequest;
import com.example.latte_api.auth.dto.AuthResponse;
import com.example.latte_api.auth.dto.RegistrationRequest;
import com.example.latte_api.role.Role;
import com.example.latte_api.role.RoleRepository;
import com.example.latte_api.role.mapper.RoleMapper;
import com.example.latte_api.security.JwtProvider;
import com.example.latte_api.user.User;
import com.example.latte_api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  private final RoleMapper roleMapper;

  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  private final JwtProvider jwtProvider;

  @Transactional
  public AuthResponse registerUser(RegistrationRequest request) {
    userRepository.findByEmailOrFirstname(request.email(), request.firstname()).ifPresent((user) -> {
      throw new IllegalArgumentException("Duplicate User");
    });

    Role role = roleRepository.findByRole(request.role()).orElseThrow(
      () -> new IllegalArgumentException("Role not exist")
    );

    User user = User.builder()
      .firstname(request.firstname())
      .email(request.email())
      .password(passwordEncoder.encode(request.password()))
      .role(role)
      .editable(true)
      .deletable(true)
      .build();

    userRepository.save(user); 

    String accessToken = jwtProvider.generateToken(user.getEmail(), Map.of());
    String refreshToken = jwtProvider.generateToken(user.getEmail(), Map.of(),  604800);

    return new AuthResponse(
      user.getFirstname(), 
      user.getEmail(), 
      accessToken, 
      refreshToken, 
      roleMapper.mapToRoleResponse(user.getRole())
    );
  }

  public AuthResponse authenticateUser(AuthRequest request) {
    try {
      Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password())
      );

      User user = (User) authentication.getPrincipal();

      String accessToken = jwtProvider.generateToken(user.getEmail(), Map.of());
      String refreshToken = jwtProvider.generateToken(user.getEmail(), Map.of(),  604800);

      return new AuthResponse(
        user.getFirstname(), 
        user.getEmail(), 
        accessToken, 
        refreshToken, 
        roleMapper.mapToRoleResponse(user.getRole())
      );
    } catch (Exception e) {
      throw new BadCredentialsException("Invalid username or passeord");
    }
    
  }

  public AuthResponse refreshToken(HttpServletRequest request) {
    String token = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (token == null || !token.startsWith("Bearer ")) {
      throw new RuntimeException("Forbidden access");
    }

    token = token.substring(7);
    String username = jwtProvider.getUsername(token);
    if (username == null) {
      throw new RuntimeException("Forbidden access");
    }

    User userDetails = userRepository.findByEmail(username).orElseThrow();

    if (!jwtProvider.validToken(userDetails, token)) {
      throw new RuntimeException("Forbidden access");
    }

    String accessToken = jwtProvider.generateToken(username, Map.of());
    return new AuthResponse(
      userDetails.getFirstname(), 
      userDetails.getUsername(), 
      accessToken, token, 
      roleMapper.mapToRoleResponse(userDetails.getRole())
    );
  }
}
