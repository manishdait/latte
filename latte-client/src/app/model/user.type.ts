import { Role } from "./role.enum";

export interface UserResponse {
  firstname: string,
  email: string,
  role: Role
}

export interface ResetPasswordRequest {
  updatePassword: string,
  confirmPassword: string
}