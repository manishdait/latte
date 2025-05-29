import { createReducer, on } from "@ngrx/store";
import { ClientResponse } from "../../model/client.type";
import { addClient, removeClient, setClientCount, setClients, updateClientCount } from "./client.action"

export interface ClientState {
  clients: ClientResponse[],
  count: number
}

export const initialState: ClientState = {
  clients: [],
  count: 0
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
  })),

  on(setClientCount, (state, {count}) => ({
    ...state,
    count: count
  })),

  on(updateClientCount, (state, {val}) => ({
    ...state,
    count: state.count + val
  }))
)
