import { Injectable } from "@angular/core";
import SockJS from "sockjs-client";
import * as Stomp from 'stompjs'
import { AuthService } from "./auth.service";
import { Observable, Subject } from "rxjs";
import { Notification } from "../model/notification.type";
import { HttpClient } from "@angular/common/http";
import { environment } from "../../environments/environment";

const URL: string = `${environment.API_ENDPOINT}/notifications`;

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private ws = new SockJS(environment.WEBSOCKET_ENDPONT)
  private socketClient = Stomp.over(this.ws);

  private message$ = new Subject<Notification>();

  constructor(private client: HttpClient, private authService: AuthService) {}

  connect() {
    this.socketClient.connect({'Authorization': `Bearer ${this.authService.getAccessToken()}`}, () => {
      this.socketClient.subscribe(`/user/${this.authService.user.email}/notification`, (message) => {
        this.message$.next(JSON.parse(message.body));
      })
    })
  }

  message(): Observable<Notification> {
    return this.message$.asObservable();
  }

  fetchNotification(): Observable<Notification[]> {
    return this.client.get<Notification[]>(URL);
  }

  deleteNotification(id: number): Observable<{[key: string]: boolean}> {
    return this.client.delete<{[key: string]: boolean}>(`${URL}/${id}`);
  }
}