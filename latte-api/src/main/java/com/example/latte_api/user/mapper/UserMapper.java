package com.example.latte_api.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

import com.example.latte_api.user.User;
import com.example.latte_api.user.dto.UserResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
  UserMapper INSTANT = Mappers.getMapper(UserMapper.class);

  @Mapping(target = "role", expression = "java(new RoleResponse(user.getRole().getId(), user.getRole().getRole(), user.getRole().getAuthorities().stream().map(a -> a.getAuthority()).toList(), user.isEditable(), user.isDeletable()))")
  UserResponse mapToUserDto(User user);
}
