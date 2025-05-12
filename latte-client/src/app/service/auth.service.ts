import { HttpBackend, HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { AuthRequest, AuthResponse, RegistrationRequest } from '../model/auth.type';
import { catchError, map, Observable, of, switchMap } from 'rxjs';
import { LocalStorageService } from 'ngx-webstorage';
import { UserResponse } from '../model/user.type';
import { UserService } from './user.service';

const URL: string = `${environment.API_ENDPOINT}/auth`;

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private _user: UserResponse = {
    firstname: '',
    email: '',
    role: {
      id: 0,
      role: '',
      editable: false,
      deletable: false,
      authorities: []
    },
    editable: false,
    deletable: false
  };

  client: HttpClient;

  constructor(private backend: HttpBackend, private localStorage: LocalStorageService, private secureClient: HttpClient, private userService: UserService) { 
    this.client = new HttpClient(backend);
  }

  authenticateUser(request: AuthRequest): Observable<AuthResponse> {
    return this.client.post<AuthResponse>(`${URL}/login`, request).pipe(
      switchMap((response) => {
        this.storeCred(response);
        return this.userService.fetchUserInfo().pipe(
          map(user => {
            this._user = user;
            return response;
          })
        )
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
    this.localStorage.store('accessToken', response.accessToken);
    this.localStorage.store('refreshToken', response.refreshToken);
  }

  getAccessToken(): string {
    return this.localStorage.retrieve('accessToken');
  }

  getFirstname(): string {
    return this.localStorage.retrieve('firstname');
  }

  isAuthenticated(): Observable<boolean> {
    const accessToken = this.localStorage.retrieve('accessToken');
    if (!accessToken) {
      return of(false);
    }

    return this.secureClient.post<{[key:string]: boolean}>(`${URL}/verify`, null).pipe(
      switchMap((response) => {
        if(response['success']) {
          return this.userService.fetchUserInfo().pipe(
            map(user => {
              this._user = user;
              return true;
            })
          )
        }

        this.localStorage.clear();
        return of(false);
      }),

      catchError((err) => {
        this.localStorage.clear();
        return of(false);
      })
    );
  }

  get user(): UserResponse {
    return this._user;
  }
}
