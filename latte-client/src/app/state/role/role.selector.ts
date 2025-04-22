import { createSelector } from "@ngrx/store";
import { AppState } from "../app.state";

export const roleStateSelector = (state: AppState) => state.roles;

export const roles = createSelector(
  roleStateSelector,
  (state) => state.roles
)

export const roleCount = createSelector(
  roleStateSelector,
  (state) => state.count
)
