import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../service/auth.service';
import { catchError, map, of } from 'rxjs';
import { SplashScreenService } from '../service/splash-screen.service';

export const homeGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const splashScreenService = inject(SplashScreenService);
  const router = inject(Router);

  splashScreenService.processing = true;
  return authService.isAuthenticated().pipe(
    map(response => {
      splashScreenService.processing = false;
      if (response) {
        return true;
      }
      router.navigate(['sign-in'], {replaceUrl: true});
      return false;
    }),

    catchError((err) => {
      splashScreenService.processing = false;
      router.navigate(['sign-in'], {replaceUrl: true});
      return of(false);
    })
  )
};
