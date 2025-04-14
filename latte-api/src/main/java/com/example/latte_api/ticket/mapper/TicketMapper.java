package com.example.latte_api.ticket.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

import com.example.latte_api.ticket.Ticket;
import com.example.latte_api.ticket.dto.TicketResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TicketMapper {
  TicketMapper INSTANT = Mappers.getMapper(TicketMapper.class);

  @Mapping(target = "createdBy.firstname", source = "createdBy.firstname")
  @Mapping(target = "createdBy.email", source = "createdBy.email")
  @Mapping(target = "createdBy.role", source = "createdBy.role.role")
  @Mapping(target = "createdBy.editable", source = "createdBy.editable")
  @Mapping(target = "assignedTo.firstname", source = "assignedTo.firstname")
  @Mapping(target = "assignedTo.email", source = "assignedTo.email")
  @Mapping(target = "assignedTo.role", source = "assignedTo.role.role")
  @Mapping(target = "assignedTo.deletable", source = "assignedTo.deletable")
  TicketResponse mapToTicketResponse(Ticket ticket);
}
