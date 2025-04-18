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
  @Mapping(target = "assignedTo.firstname", source = "assignedTo.firstname")
  @Mapping(target = "assignedTo.email", source = "assignedTo.email")
  TicketResponse mapToTicketResponse(Ticket ticket);
}
