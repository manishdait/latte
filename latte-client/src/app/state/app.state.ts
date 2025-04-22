import { RoleState } from "./role/role.reducer";
import { TicketState } from "./ticket/ticket.reducer";
import { UserState } from "./user/user.reducer";

export interface AppState {
  tickets: TicketState,
  users: UserState,
  roles: RoleState
}

