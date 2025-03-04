package com.example.latte_api.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.latte_api.security.JwtFilter;
import com.example.latte_api.user.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Autowired
  private UserService userService;

  @Autowired
  private JwtFilter jwtFilter;

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable());
    http.cors(cors -> cors.configurationSource(configurationSource()));

    http.authorizeHttpRequests((request) -> {
      request.requestMatchers(
        "/error",
        "/favicon.ico",
        "/latte-api/v1/auth/login",
        "/latte-api/v1/auth/refresh"
      ).permitAll();

      request.requestMatchers(
        HttpMethod.GET, 
        "/latte-api/v1/users",
        "/latte-api/v1/users/info/*",
        "/latte-api/v1/users/count"
      ).hasRole("ADMIN");

      request.requestMatchers(
        HttpMethod.POST, 
        "/latte-api/v1/auth/sign-up"
      ).hasRole("ADMIN");

      request.requestMatchers(
        HttpMethod.PUT, 
        "/latte-api/v1/users/*"
      ).hasRole("ADMIN");

      request.requestMatchers(
        HttpMethod.PATCH, 
        "/latte-api/v1/users/*",
        "/latte-api/v1/tickets/lock/*",
        "/latte-api/v1/tickets/unlock/*"
      ).hasRole("ADMIN");

      request.requestMatchers(
        HttpMethod.DELETE, 
        "/latte-api/v1/users/**",
        "/latte-api/v1/tickets/**"
      ).hasRole("ADMIN");
      

      request.anyRequest().authenticated();
    });
    http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.authenticationProvider(authenticationProvider());
    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setUserDetailsService(userService);
    authenticationProvider.setPasswordEncoder(passwordEncoder());
    return authenticationProvider;
  }

  @Bean
  CorsConfigurationSource configurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("*"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.ORIGIN));
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**" ,configuration);
    return source;
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
