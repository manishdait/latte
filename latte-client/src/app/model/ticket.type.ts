import { Priority } from "./priority.type";
import { Status } from "./status.type";
import { UserDto } from "./user.type";

export interface TicketResponse {
  readonly id: number,
  title: string,
  description: string,
  priority: Priority,
  status: Status,
  lock: boolean,
  createdBy: UserDto,
  assignedTo: UserDto | null,
  createdAt: Date,
  lastUpdated: Date,
  clientName: string | null,
  clientEmail: string | null
}

export interface TicketRequest {
  title: string,
  description: string,
  priority: Priority,
  status: Status,
  assignedTo: string,
  clientId: number
}

export interface PatchTicketRequest {
  title: string | null,
  description: string | null,
  priority: Priority | null,
  status: Status | null,
  assignedTo: string | null,
  clientId: number | null
}
