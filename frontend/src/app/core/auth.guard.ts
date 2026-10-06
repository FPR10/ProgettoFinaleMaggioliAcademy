import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (_route, state) => {
  const router = inject(Router);
  const login = router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  return inject(AuthService).checkSession().pipe(map(authenticated => authenticated || login), catchError(() => of(login)));
};
