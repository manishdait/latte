import { createReducer, on } from "@ngrx/store";
import { TicketResponse } from "../../model/ticket.type";
import { 
  addTicket,
  removeTicket,
  setCloseCount,
  setOpenCount,
  setTicketCount,
  setTickets, 
  UpdateCloseCount, 
  updateOpenCount, 
  updateTicketCount
} from "./ticket.action";

export interface TicketState {
  tickets: TicketResponse[],
  total: number,
  open: number,
  close: number
}

export const initialTicketState: TicketState = {
  tickets: [],
  total: 0,
  open: 0,
  close: 0
}

export const ticketReducer = createReducer(
  initialTicketState,

  on(setTickets, (state, {tickets}) => ({
    ...state,
    tickets: tickets
  })),

  on(addTicket, (state, {ticket}) => ({
    ...state,
    tickets: [ticket, ...state.tickets]
  })),

  on(removeTicket, (state, {ticketId}) => ({
    ...state,
    tickets: [...state.tickets.filter(ticket => ticket.id !== ticketId)]
  })),

  on(setTicketCount, (state, {count}) => ({
    ...state,
    total: count
  })),

  on(updateTicketCount, (state, {count}) => ({
    ...state,
    total: state.total + count
  })),

  on(setOpenCount, (state, {count}) => ({
    ...state,
    open: count
  })),

  on(updateOpenCount, (state, {count}) => ({
    ...state,
    open: state.open + count
  })),

  on(setCloseCount, (state, {count}) => ({
    ...state,
    close: count
  })),

  on(UpdateCloseCount, (state, {count}) => ({
    ...state,
    close: state.close + count
  }))
);

