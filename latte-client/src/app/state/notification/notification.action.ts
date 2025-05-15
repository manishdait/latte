import { createAction, props } from "@ngrx/store";
import { Notification } from "../../model/notification.type";

export const setNotification = createAction('[Notification] Set notification', props<{notifications: Notification[]}>());
export const addNotification = createAction('[Notification] Add notification', props<{notification: Notification}>());
export const removeNotification = createAction('[Notification] Remove notification');
