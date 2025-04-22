import { HttpBackend, HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { AuthRequest, AuthResponse, RegistrationRequest } from '../model/auth.type';
import { catchError, map, Observable, of } from 'rxjs';
import { LocalStorageService } from 'ngx-webstorage';
import { Role } from '../model/role.type';

const URL: string = `${environment.API_ENDPOINT}/auth`;

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  client: HttpClient;

  constructor(private backend: HttpBackend, private localStorage: LocalStorageService, private secureClient: HttpClient) { 
    this.client = new HttpClient(backend);
  }

  authenticateUser(request: AuthRequest): Observable<AuthResponse> {
    return this.client.post<AuthResponse>(`${URL}/login`, request).pipe(
      map((response) => {
        this.storeCred(response);
        return response;
      })
    );
  }

  registerUser(request: RegistrationRequest): Observable<AuthResponse> {
    return this.secureClient.post<AuthResponse>(`${URL}/sign-up`, request);
  }

  refreshToken(): Observable<AuthResponse> {
    const refreshToken: string = this.localStorage.retrieve('refreshToken');
    return this.client.post<AuthResponse>(`${URL}/refresh`, null, {headers: {Authorization: `Bearer ${refreshToken}`}}).pipe(
      map((response) => {
        this.storeCred(response);
        return response;
      })
    )
  }

  logout(): void {
    this.localStorage.clear();
  }

  private storeCred(response: AuthResponse): void {
    this.localStorage.store('firstname', response.firstname);
    this.localStorage.store('username', response.email);
    this.localStorage.store('accessToken', response.accessToken);
    this.localStorage.store('refreshToken', response.refreshToken);
    this.localStorage.store('role', response.role);
  }

  getAccessToken(): string {
    return this.localStorage.retrieve('accessToken');
  }

  getRole(): Role {
    return this.localStorage.retrieve('role');
  }

  getFirstname(): string {
    return this.localStorage.retrieve('firstname');
  }

  isAuthenticated(): Observable<boolean> {
    const accessToken = this.localStorage.retrieve('accessToken');
    if (!accessToken) {
      return of(false);
    }

    return this.secureClient.post<Map<string, boolean>>(`${URL}/verify`, null).pipe(
      map((response) => {
        const result = response as any;
        if(result.success) {
          return true;
        }
        this.localStorage.clear();
        return false;
      }),

      catchError((err) => {
        this.localStorage.clear();
        return of(false);
      })
    );
  }

  createUser() {
    return this.getRole().authorities.includes('user::create');
  }
  
  editUser() {
    return this.getRole().authorities.includes('user::edit');
  }
  
  deleteUser() {
    return this.getRole().authorities.includes('user::delete');
  }
  
  resetUserPassword() {
    return this.getRole().authorities.includes('user::reset-password');
  }
  
  createTicket() {
    return this.getRole().authorities.includes('ticket::create');
  }
  
  editTicket() {
    return this.getRole().authorities.includes('ticket::edit');
  }
  
  deleteTicket() {
    return this.getRole().authorities.includes('ticket::delete');
  }
  
  assignTicket() {
    return this.getRole().authorities.includes('ticket::assign');
  }
  
  lockTicket() {
    return this.getRole().authorities.includes('ticket::lock-unlock');
  }
  
  createRole() {
    return this.getRole().authorities.includes('role::create');
  }
  
  editRole() {
    return this.getRole().authorities.includes('role::edit');
  }
  
  deleteRole() {
    return this.getRole().authorities.includes('role::delete');
  }
}
