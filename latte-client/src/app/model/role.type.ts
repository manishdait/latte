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

export type Authority =  'user::create' |'user::edit' | 'user::delete' | 'user::reset-password' | 'ticket::create' | 'ticket::edit' 
  | 'ticket::delete' | 'ticket::lock-unlock' | 'ticket::assign' | 'role::create' | 'role::edit' | 'role::delete';

