import { createSelector } from "@ngrx/store";
import { AppState } from "../app.state";

export const selectUserstate = (state: AppState) => state.users;
export const selectUserCountState = (state: AppState) => state.userCount;

export const userSelector = createSelector(
  selectUserstate,
  (state) => state.users
);

export const userCountSelector = createSelector(
  selectUserCountState,
  (state) => state.count
);
