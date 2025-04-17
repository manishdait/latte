import { Injectable } from "@angular/core";
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { Page } from "../model/page.type";
import { ActivityResponse } from "../model/activity.type";

const URL: string = `${environment.API_ENDPOINT}/activities`;

@Injectable({
  providedIn: 'root'
})
export class ActivityService {
  constructor(private client: HttpClient) {}

  getActivitiesForTicket(id: number, page: number, size: number):Observable<Page<ActivityResponse>> {
    return this.client.get<Page<ActivityResponse>>(`${URL}/ticket/${id}?page=${page}&size=${size}`);
  }
}