import { Injectable } from "@angular/core";
import { environment } from "../../environments/environment";
import { CommentRequest } from "../model/comment.type";
import { Observable } from "rxjs";
import { ActivityResponse } from "../model/activity.type";
import { HttpClient } from "@angular/common/http";

const URL: string = `${environment.API_ENDPOINT}/comments`;

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  constructor(private client: HttpClient) {}

  createComment(request: CommentRequest): Observable<ActivityResponse> {
    return this.client.post<ActivityResponse>(`${URL}`, request);
  }
}