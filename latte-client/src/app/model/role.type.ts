import { Authority } from "./authority.type";

export interface Role {
  readonly id: number,
  role: string,
  editable: boolean,
  deletable: boolean,
  authorities: Authority[]
}

export interface RoleRequest {
  role: string | null,
  authorities: Authority[] | null
}