import { Role } from "./role.type"

export interface RegistrationRequest {
  firstname: string,
  email: string,
  password: string
  role: Role
}

export interface AuthRequest {
  email: string,
  password: string
}

export interface AuthResponse {
  firstname: string,
  email: string,
  accessToken: string,
  refreshToken: string,
  role: Role
}
