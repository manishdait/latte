import { createAction, props } from "@ngrx/store";
import { Role } from "../../model/role.type";

export const setRoles = createAction('[Role] Set roles', props<{roles: Role[]}>());
export const addRole = createAction('[Role] Add role', props<{role: Role}>());
export const removeRole = createAction('[Role] Remove role', props<{roleId: number}>());

export const setRoleCount = createAction('[Role Count] Set role count', props<{count: number}>());
export const updateRoleCount = createAction('[Role Count] Update role count', props<{count: number}>());
