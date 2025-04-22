import { createAction, props } from "@ngrx/store";
import { UserResponse } from "../../model/user.type";

export const setUsers = createAction('[User] Set User', props<{users: UserResponse[]}>());
export const addUser = createAction('[User] Add User', props<{user: UserResponse}>());
export const removeUser = createAction('[User] Remove User', props<{email: string}>());

export const setUserCount = createAction('[User Count] Set UserCount', props<{count: number}>());
export const updateUserCount = createAction('[User Count] Update UserCount', props<{count: number}>());
