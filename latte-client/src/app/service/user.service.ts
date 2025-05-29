import { Injectable } from "@angular/core";
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { ResetPasswordRequest, UserResponse } from "../model/user.type";
import { Page } from "../model/page.type";

const URL: string = `${environment.API_ENDPOINT}/users`;

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private client: HttpClient) {}

  fetchPagedUsers(pageNumber: number, size: number): Observable<Page<UserResponse>> {
    return this.client.get<Page<UserResponse>>(`${URL}?page=${pageNumber}&size=${size}`);
  }

  fetchUsers(): Observable<Page<UserResponse>> {
    return this.fetchPagedUsers(0, 10);
  }

  fetchUserList(pageNumber: number, size: number): Observable<Page<string>> {
    return this.client.get<Page<string>>(`${URL}/list?page=${pageNumber}&size=${size}`);
  }

  fetchUserInfo(): Observable<UserResponse> {
    return this.client.get<UserResponse>(`${URL}/info`);
  }

  updateUser(request: UserResponse): Observable<UserResponse> {
    return this.client.put<UserResponse>(`${URL}`, request);
  }

  editUser(request: UserResponse, _user:string): Observable<UserResponse> {
    return this.client.put<UserResponse>(`${URL}/${_user}`, request);
  }

  resetPassword(request: ResetPasswordRequest): Observable<UserResponse> {
    return this.client.patch<UserResponse>(`${URL}`, request);
  }

  resetPasswordForUser(request: ResetPasswordRequest, _user: string): Observable<UserResponse> {
    return this.client.patch<UserResponse>(`${URL}/${_user}`, request);
  }

  deleteUser(_user:string): Observable<{[key: string]: any}> {
    return this.client.delete<{[key: string]: any}>(`${URL}/${_user}`);
  }
}