import { authority } from "./authority.type";

export interface Role {
  readonly id: number,
  role: string,
  authorities: authority[]
}

export interface RoleRequest {
  role: string,
  authorities: authority[]
}