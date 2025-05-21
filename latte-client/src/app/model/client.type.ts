export interface ClientResponse {
  readonly id: number,
  name: string,
  email: string,
  phone: string,
  deletable: boolean
}

export interface ClientRequest {
  name: string,
  email: string,
  phone: string
}
