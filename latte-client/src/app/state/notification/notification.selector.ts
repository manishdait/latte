import { createSelector } from "@ngrx/store";
import { AppState } from "../app.state";

export const notificationStateSelector = (state: AppState) => state.notifications;

export const notifications = createSelector(
  notificationStateSelector,
  (state) => state.notifications
)
