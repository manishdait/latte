import { createAction, props } from "@ngrx/store";
import { TicketResponse } from "../../models/ticket.type";

export const addTicket = createAction('[Ticket] Add', props<{ticket: TicketResponse}>());
export const setTickets = createAction('[Ticket] Set', props<{tickets: TicketResponse[]}>());

export const setTicketOpenCount = createAction('[TicketOpenCount] Set', props<{ticketCount: number}>());
export const incrementTicketOpenCount = createAction('[TicketOpenCount] Increment');
export const decrementTicketOpenCount = createAction('[TicketOpenCount] Decrement');

export const setTicketCloseCount = createAction('[TicketCloseCount] Set', props<{ticketCount: number}>());
export const incrementTicketCloseCount = createAction('[TicketCloseCount] Increment');
export const decrementTicketCloseCount = createAction('[TicketCLoseCount] Decrement');
