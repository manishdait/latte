import { createAction, props } from "@ngrx/store";
import { ClientResponse } from "../../model/client.type";

export const setClients = createAction('[Client] Set clients', props<{clients: ClientResponse[]}>());
export const addClient = createAction('[Client] Add client', props<{client: ClientResponse}>());
export const removeClient = createAction('[Client] Remove client', props<{id: number}>());

export const setClientCount = createAction('[Client Count] Set client count', props<{count: number}>());
export const updateClientCount = createAction('[Client Count] Update count', props<{val: number}>());
