import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideState, provideStore } from '@ngrx/store';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { AccessTokenInterceptor } from './interceptor/access-token.interceptor';
import { RefreshTokenInterceptor } from './interceptor/refresh-token.interceptor';
import { ticketCloseCountReducer, ticketOpenCountReducer, ticketReducer } from './state/ticket/ticket.reducer';
import { userCountReducer, userReducer } from './state/user/user.reducer';
import { provideNgxWebstorage, withLocalStorage } from 'ngx-webstorage';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideStore(),
    provideState({name: 'tickets', reducer: ticketReducer}),
    provideState({name: 'ticketOpenCount', reducer: ticketOpenCountReducer}),
    provideState({name: 'ticketCloseCount', reducer: ticketCloseCountReducer}),
    provideState({name: 'users', reducer: userReducer}),
    provideState({name: 'userCount', reducer: userCountReducer}),
    provideNgxWebstorage(withLocalStorage()),
    provideHttpClient(withInterceptorsFromDi()),
    [
      { provide: HTTP_INTERCEPTORS, useClass: RefreshTokenInterceptor, multi: true },
      { provide: HTTP_INTERCEPTORS, useClass: AccessTokenInterceptor, multi: true }
    ]
  ]
};
