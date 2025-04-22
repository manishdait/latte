import { Injectable } from "@angular/core";
import { environment } from "../../environments/environment";
import { CommentDto } from "../model/comment.type";
import { Observable } from "rxjs";
import { ActivityResponse } from "../model/activity.type";
import { HttpClient } from "@angular/common/http";

const URL: string = `${environment.API_ENDPOINT}/comments`;

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  constructor(private client: HttpClient) {}

  createComment(request: CommentDto): Observable<ActivityResponse> {
    return this.client.post<ActivityResponse>(`${URL}`, request);
  }

  updateCommnet(id: number, request: CommentDto): Observable<ActivityResponse> {
    return this.client.patch<ActivityResponse>(`${URL}/${id}`, request);
  }

  deleteComment(id: number): Observable<{ [key: string]: boolean }> {
    return this.client.delete<{ [key: string]: boolean }>(`${URL}/${id}`);
  }
}