import { createSelector } from "@ngrx/store";
import { AppState } from "../app.state";

export const selectUserState = (state: AppState) => state.users;

export const users = createSelector(
  selectUserState,
  (state) => state.users
);

export const userCount = createSelector(
  selectUserState,
  (state) => state.count
);
