import { createSelector } from "@ngrx/store";
import { AppState } from "../app.state";

export const selectTicketState = (state: AppState) => state.tickets;

export const tickets = createSelector(
  selectTicketState,
  (state) => state.tickets
);

export const totalTickets = createSelector(
  selectTicketState,
  (state) => state.total
);

export const openTickets = createSelector(
  selectTicketState,
  (state) => state.open
);

export const closeTickets = createSelector(
  selectTicketState,
  (state) => state.close
);
