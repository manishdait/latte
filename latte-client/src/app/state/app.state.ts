import { TicketCount, TicketState } from "./ticket/ticket.reducer";
import { UserCount, UserState } from "./user/user.reducer";

export interface AppState {
  tickets: TicketState,
  ticketOpenCount: TicketCount,
  ticketCloseCount: TicketCount,
  users: UserState,
  userCount: UserCount
}
