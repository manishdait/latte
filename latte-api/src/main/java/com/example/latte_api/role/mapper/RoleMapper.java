package com.example.latte_api.role.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

import com.example.latte_api.role.Role;
import com.example.latte_api.role.dto.RoleResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoleMapper {
  RoleMapper INSTANT = Mappers.getMapper(RoleMapper.class);

  @Mapping(target = "authorities", expression = "java(role.getAuthorities().stream().map(a -> a.getAuthority()).toList())")
  RoleResponse mapToRoleResponse(Role role);
}
