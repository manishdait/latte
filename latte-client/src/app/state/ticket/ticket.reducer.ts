import { createReducer, on } from "@ngrx/store";
import { TicketResponse } from "../../model/ticket.type";
import { 
  addTicket, 
  decrementTicketCloseCount, 
  decrementTicketOpenCount, 
  incrementTicketCloseCount, 
  incrementTicketOpenCount, 
  setTicketCloseCount, 
  setTicketOpenCount, 
  setTickets 
} from "./ticket.action";

export interface TicketState {
  tickets: TicketResponse[]
}

export interface TicketCount {
  ticketCount: number
}

export const initialTicketState: TicketState = {
  tickets: []
}

export const initialTicketOpenCountState: TicketCount = {
  ticketCount: 0
}

export const initialTicketCloseCountState: TicketCount = {
  ticketCount: 0
}

export const ticketReducer = createReducer(
  initialTicketState,
  
  on(addTicket, (state, {ticket}) => ({
    ...state,
    tickets: [ticket, ...state.tickets]
  })),

  on(setTickets, (state, {tickets}) => ({
    ...state,
    tickets: tickets
  }))
);

export const ticketOpenCountReducer = createReducer(
  initialTicketOpenCountState,

  on(setTicketOpenCount, (state, {ticketCount}) => ({
    ...state,
    ticketCount: ticketCount
  })),

  on(incrementTicketOpenCount, (state) => ({
    ...state,
    ticketCount: state.ticketCount + 1
  })),

  on(decrementTicketOpenCount, (state) => ({
    ...state,
    ticketCount: state.ticketCount - 1
  }))
);

export const ticketCloseCountReducer = createReducer(
  initialTicketCloseCountState,

  on(setTicketCloseCount, (state, {ticketCount}) => ({
    ...state,
    ticketCount: ticketCount
  })),

  on(incrementTicketCloseCount, (state) => ({
    ...state,
    ticketCount: state.ticketCount + 1
  })),

  on(decrementTicketCloseCount, (state) => ({
    ...state,
    ticketCount: state.ticketCount - 1
  }))
);
