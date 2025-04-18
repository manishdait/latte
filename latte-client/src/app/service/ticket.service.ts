import { Injectable } from "@angular/core";
import { environment } from "../../environments/environment";
import { Observable } from "rxjs";
import { Page } from "../model/page.type";
import { PatchTicketRequest, TicketRequest, TicketResponse } from "../model/ticket.type";
import { HttpClient } from "@angular/common/http";
import { Status } from "../model/status.enum";

const URL: string = `${environment.API_ENDPOINT}/tickets`;

@Injectable({
  providedIn: 'root'
})
export class TicketService {
  constructor(private client: HttpClient) {}

  fetchTicktsInfo(): Observable<{ [key: string]: number }> {
    return this.client.get<{ [key: string]: number }>(`${URL}/info`);
  }

  fetchPagedTickets(page: number, size: number): Observable<Page<TicketResponse>> {
    return this.client.get<Page<TicketResponse>>(`${URL}?page=${page}&size=${size}`);
  }

  fetchPagedTicketsByStaus(status: Status, page: number, size: number): Observable<Page<TicketResponse>> {
    return this.client.get<Page<TicketResponse>>(`${URL}/status/${status}?page=${page}&size=${size}`);
  }

  fetchTickets(): Observable<Page<TicketResponse>> {
    return this.fetchPagedTickets(0, 10);
  }

  fetchTicket(id: number): Observable<TicketResponse> {
    return this.client.get<TicketResponse>(`${URL}/${id}`);
  }

  createTicket(request: TicketRequest): Observable<TicketResponse> {
    return this.client.post<TicketResponse>(`${URL}`, request);
  }

  updateTicket(ticketId: number, request: PatchTicketRequest): Observable<TicketResponse> {
    return this.client.patch<TicketResponse>(`${URL}/${ticketId}`, request);
  }

  lockTicket(ticketId: number) {
    return this.client.patch<TicketResponse>(`${URL}/lock/${ticketId}`, null);
  }

  unlockTicket(ticketId: number) {
    return this.client.patch<TicketResponse>(`${URL}/unlock/${ticketId}`, null);
  }

  deleteTicket(ticketId: number): Observable<Map<string, any>> {
    return this.client.delete<Map<string, any>>(`${URL}/${ticketId}`);
  }
}