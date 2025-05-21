import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideState, provideStore } from '@ngrx/store';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { AccessTokenInterceptor } from './interceptor/access-token.interceptor';
import { RefreshTokenInterceptor } from './interceptor/refresh-token.interceptor';
import { ticketReducer } from './state/ticket/ticket.reducer';
import { userReducer } from './state/user/user.reducer';
import { provideNgxWebstorage, withLocalStorage } from 'ngx-webstorage';
import { roleReducer } from './state/role/role.reducer';
import { notificationReducer } from './state/notification/notification.reducer';
import { clientReducer } from './state/client/client.reducer';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideStore(),
    provideState({name: 'tickets', reducer: ticketReducer}),
    provideState({name: 'users', reducer: userReducer}),
    provideState({name: 'roles', reducer: roleReducer}),
    provideState({name: 'notifications', reducer: notificationReducer}),
    provideState({name: 'clients', reducer: clientReducer}),
    provideNgxWebstorage(withLocalStorage()),
    provideHttpClient(withInterceptorsFromDi()),
    [
      { provide: HTTP_INTERCEPTORS, useClass: RefreshTokenInterceptor, multi: true },
      { provide: HTTP_INTERCEPTORS, useClass: AccessTokenInterceptor, multi: true }
    ]
  ]
};
