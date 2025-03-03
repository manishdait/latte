import { createSelector } from "@ngrx/store";
import { AppState } from "../app.state";

export const selectTicketState = (state: AppState) => state.tickets;
export const selectTicketOpenCountState = (state: AppState) => state.ticketOpenCount;
export const selectTicketCloseCountState = (state: AppState) => state.ticketCloseCount;

export const selectTickets = createSelector(
  selectTicketState,
  (state) => state.tickets
);

export const selectTicketOpenCount = createSelector(
  selectTicketOpenCountState,
  (state) => state.ticketCount
);

export const selectTicketCloseCount = createSelector(
  selectTicketCloseCountState,
  (state) => state.ticketCount
);

