import { createReducer, on } from "@ngrx/store";
import { Role } from "../../model/role.type";
import { addRole, removeRole, setRoleCount, setRoles, updateRoleCount } from "./role.action";

export interface RoleState {
  roles: Role[],
  count: number
}

export const initialRoleState: RoleState = {
  roles: [],
  count: 0
}

export const roleReducer = createReducer(
  initialRoleState,

  on(setRoles, (state, {roles}) => ({
    ...state,
    roles: roles
  })),

  on(addRole, (state, {role}) => ({
    ...state,
    roles: [role, ...state.roles]
  })),

  on(removeRole, (state, {roleId}) => ({
    ...state,
    roles: [...state.roles.filter(role => role.id !== roleId)]
  })),

  on(setRoleCount, (state, {count}) => ({
    ...state,
    count: count
  })),

  on(updateRoleCount, (state, {count}) => ({
    ...state,
    count: state.count + count
  }))
);
