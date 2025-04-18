import { Injectable, signal } from "@angular/core";
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { map, Observable } from "rxjs";
import { Role, RoleRequest } from "../model/role.enum";

const URL: string = `${environment.API_ENDPOINT}/roles`;

@Injectable({
  providedIn: 'root'
})
export class RoleService {
  constructor(private client: HttpClient) {
  }

  getRoles() : Observable<Role[]> {
    return this.client.get<Role[]>(`${URL}`);
  }

  createRole(request: RoleRequest): Observable<Role> {
    return this.client.post<Role>(`${URL}`, request);
  }
}