import { Injectable } from "@angular/core";
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { Role, RoleRequest } from "../model/role.type";
import { Page } from "../model/page.type";

const URL: string = `${environment.API_ENDPOINT}/roles`;

@Injectable({
  providedIn: 'root'
})
export class RoleService {
  constructor(private client: HttpClient) {}

  createRole(request: RoleRequest): Observable<Role> {
    return this.client.post<Role>(`${URL}`, request);
  }

  getRoles(page: number, size: number): Observable<Page<Role>> {
    return this.client.get<Page<Role>>(`${URL}?page=${page}&size=${size}`);
  }

  getRole(id: number): Observable<Role> {
    return this.client.get<Role>(`${URL}/${id}`);
  }


  updateRole(id: number, request: RoleRequest): Observable<Role> {
    return this.client.patch<Role>(`${URL}/${id}`, request);
  }

  deleteRole(id: number, updateRole: number): Observable<{[key: string]: boolean}> {
    return this.client.delete<{[key: string]: boolean}>(`${URL}/${id}/update-to/${updateRole}`);
  }
}