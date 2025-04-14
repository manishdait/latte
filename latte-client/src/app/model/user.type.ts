import { Role } from "./role.enum";

export interface UserResponse {
  firstname: string,
  email: string,
  role: Role,
  editable: boolean,
  deletable: boolean,
}

export interface ResetPasswordRequest {
  updatePassword: string,
  confirmPassword: string
}