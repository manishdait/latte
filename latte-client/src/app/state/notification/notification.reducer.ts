import { createReducer, on } from "@ngrx/store"
import { addNotification, setNotification, setRecentNotification } from "./notification.action";
import { Notification } from "../../model/notification.type";

export interface NotificationState {
  notifications: Notification[],
  recentNotification: boolean
}

const initialState: NotificationState = {
  notifications: [],
  recentNotification: false
}

export const notificationReducer = createReducer(
  initialState,

  on(setNotification, (state, {notifications}) => ({
    ...state,
    notifications: notifications
  })),

  on(addNotification, (state, {notification}) => ({
    ...state,
    notifications: [notification, ...state.notifications]
  })),

  on(setRecentNotification, (state, {status}) => ({
    ...state,
    recentNotification: status
  }))
);
