import { Role } from "./role.enum";

export interface UserResponse {
  firstname: string,
  email: string,
  role: Role,
  editable: boolean,
  deletable: boolean,
}

export interface UserRequest {
  firstname: string,
  email: string,
  role: string
}

export interface ResetPasswordRequest {
  updatePassword: string,
  confirmPassword: string
}

export interface UserDto {
  firstname: string,
  email: string
}
