import { createAction, props } from "@ngrx/store";
import { TicketResponse } from "../../model/ticket.type";

export const setTickets = createAction('[Ticket] Set tickets', props<{tickets: TicketResponse[]}>());
export const addTicket = createAction('[Ticket] Add tickets', props<{ticket: TicketResponse}>());
export const removeTicket = createAction('[Ticket] Remove tickets', props<{ticketId: number}>());

export const setTicketCount = createAction('[Ticket Count] Set ticket count', props<{count: number}>());
export const updateTicketCount = createAction('[Ticket Count] Update ticket count', props<{count: number}>());

export const setOpenCount = createAction('[Ticket Count] Set open ticket count', props<{count: number}>());
export const updateOpenCount = createAction('[Ticket Count] Update open ticket count', props<{count: number}>());

export const setCloseCount = createAction('[Ticket Count] Set closed ticket count', props<{count: number}>());
export const UpdateCloseCount = createAction('[Ticket Count] Update closed ticket count', props<{count: number}>());
