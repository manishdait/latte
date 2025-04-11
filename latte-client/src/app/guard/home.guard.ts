import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../service/auth.service';
import { catchError, map, of } from 'rxjs';

export const homeGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.isAuthenticated().pipe(
    map(response => {
      if (response) {
        return true;
      }
      router.navigate(['sign-in'], {replaceUrl: true});
      return false;
    }),

    catchError((err) => {
      router.navigate(['sign-in'], {replaceUrl: true});
      return of(false);
    })
  )
};
