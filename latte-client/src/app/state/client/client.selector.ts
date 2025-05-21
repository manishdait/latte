import { createSelector } from "@ngrx/store";
import { AppState } from "../app.state";

export const clientStateSelector = (state: AppState) => state.clients;

export const client = createSelector(
  clientStateSelector,
  (state) => state.clients
)
