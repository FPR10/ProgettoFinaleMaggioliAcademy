import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from './notification.service';
import { Router } from '@angular/router';

export const errorInterceptor: HttpInterceptorFn = (request, next) => {
  const notifications = inject(NotificationService);
  const router = inject(Router);
  return next(request).pipe(catchError((error: HttpErrorResponse) => {
    if (error.status === 0) notifications.show('Backend non raggiungibile');
    else if (error.status === 401 && request.url.startsWith('/api/anagrafiche')) {
      notifications.show('Sessione scaduta. Accedi nuovamente.');
      void router.navigate(['/login'], { queryParams: { returnUrl: router.url } });
    }
    else if (error.status >= 500) notifications.show(`Errore del server (${error.status})`);
    return throwError(() => error);
  }));
};
