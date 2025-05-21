import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "../../environments/environment";
import { Observable } from "rxjs";
import { Page } from "../model/page.type";
import { ClientRequest, ClientResponse } from "../model/client.type";

const URL = `${environment.API_ENDPOINT}/clients`

@Injectable({
  providedIn: 'root'
})
export class ClientService {
  constructor(private client: HttpClient) {}
  
  fetchClients(page: number, size: number): Observable<Page<ClientResponse>> {
    return this.client.get<Page<ClientResponse>>(`${URL}?page=${page}&size=${size}`);
  }

  createClient(request: ClientRequest): Observable<ClientResponse> {
    return this.client.post<ClientResponse>(URL, request);
  }

  updateClient(id: number, request: ClientRequest): Observable<ClientResponse> {
    return this.client.put<ClientResponse>(`${URL}/${id}`, request);
  }

  deleteClient(id: number): Observable<{[key: string]: boolean}> {
    return this.client.delete<{[key: string]: boolean}>(`${URL}/${id}`);
  }
}
