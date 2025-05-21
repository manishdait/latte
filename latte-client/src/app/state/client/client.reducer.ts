import { createReducer, on } from "@ngrx/store";
import { ClientResponse } from "../../model/client.type";
import { addClient, removeClient, setClients } from "./client.action"

export interface ClientState {
  clients: ClientResponse[]
}

export const initialState: ClientState = {
  clients: []
}

export const clientReducer = createReducer(
  initialState,

  on(setClients, (state,{clients}) => ({
    ...state,
    clients: clients
  })),

  on(addClient, (state,{client}) => ({
    ...state,
    clients: [client, ...state.clients]
  })),

  on(removeClient, (state,{id}) => ({
    ...state,
    clients: [...state.clients.filter(client => client.id != id)]
  }))
)
