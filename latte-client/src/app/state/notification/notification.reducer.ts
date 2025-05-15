import { createReducer, on } from "@ngrx/store"
import { addNotification, setNotification } from "./notification.action";
import { Notification } from "../../model/notification.type";

export interface NotificationState {
  notifications: Notification[]
}

const initialState: NotificationState = {
  notifications: []
}

export const notificationReducer = createReducer(
  initialState,

  on(setNotification, (state, {notifications}) => ({
    ...state,
    notifications: notifications
  })),

  on(addNotification, (state, {notification}) => ({
    ...state,
    notifications: [...state.notifications, notification]
  }))
);
