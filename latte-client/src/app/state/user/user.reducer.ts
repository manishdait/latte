import { createReducer, on } from "@ngrx/store";
import { UserResponse } from "../../model/user.type";
import { addUser, decrementUserCount, incrementUserCount, removeUser, setUserCount, setUsers } from "./user.action";

export interface UserState {
  users: UserResponse[]
}

export interface UserCount {
  count: number
}

export const initialUserState: UserState = {
  users: []
}

export const initialUserCount: UserCount = {
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
  }))
)

export const userCountReducer = createReducer(
  initialUserCount,

  on(setUserCount, (state, {userCount}) => ({
    ...state,
    count: userCount
  })),

  on(incrementUserCount, (state) => ({
    ...state,
    count: state.count + 1
  })),

  on(decrementUserCount, (state) => ({
    ...state,
    count: state.count - 1
  })),
);
