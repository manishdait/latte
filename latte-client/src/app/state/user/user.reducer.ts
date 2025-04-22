import { createReducer, on } from "@ngrx/store";
import { UserResponse } from "../../model/user.type";
import { addUser, removeUser, setUserCount, setUsers, updateUserCount } from "./user.action";

export interface UserState {
  users: UserResponse[],
  count: number
}

export const initialUserState: UserState = {
  users: [],
  count: 0
}

export const userReducer = createReducer(
  initialUserState,

  on(addUser, (state, {user}) => ({
    ...state,
    users: [user, ...state.users]
  })),

  on(setUsers, (state, {users}) => ({
    ...state,
    users: users
  })),

  on(removeUser, (state, {email}) => ({
    ...state,
    users: [...state.users.filter(u => u.email !== email)]
  })),

  on(setUserCount, (state, {count}) => ({
    ...state,
    count: count
  })),

  on(updateUserCount, (state, {count}) =>({
    ...state,
    count: count + state.count
  }))
);
