import { createAction, props } from "@ngrx/store";
import { Notification } from "../../model/notification.type";

export const setNotification = createAction('[Notification] Set notification', props<{notifications: Notification[]}>());
export const addNotification = createAction('[Notification] Add notification', props<{notification: Notification}>());

export const setRecentNotification = createAction('[Notification] Set recent notification', props<{status: boolean}>());
