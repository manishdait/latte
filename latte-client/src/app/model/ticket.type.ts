import { Priority } from "./priority.enum";
import { Status } from "./status.enum";
import { UserResponse } from "./user.type";

export interface TicketResponse {
  id: number,
  title: string,
  description: string,
  priority: Priority,
  status: Status,
  lock: boolean,
  createdBy: UserResponse,
  assignedTo: UserResponse | null,
  createdAt: Date,
  lastUpdated: Date
}

export interface TicketRequest {
  title: string,
  description: string,
  priority: Priority,
  status: Status,
  assignedTo: string
}

export interface PatchTicketRequest {
  title: string | null,
  description: string | null,
  priority: Priority | null,
  status: Status | null,
  assignedTo: string | null
}