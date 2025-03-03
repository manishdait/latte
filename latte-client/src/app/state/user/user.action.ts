import { createAction, props } from "@ngrx/store";
import { UserResponse } from "../../models/user.type";

export const addUser = createAction('[User] Add', props<{user: UserResponse}>());
export const setUsers = createAction('[User] Set', props<{users: UserResponse[]}>());
export const removeUser = createAction('[User] Remove', props<{email: string}>());

export const setUserCount = createAction('[User Count] Set', props<{userCount: number}>());
export const incrementUserCount = createAction('[User Count] Increment');
export const decrementUserCount = createAction('[User Count] Decrement');
