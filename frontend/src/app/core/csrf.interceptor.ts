import { HttpClient, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { switchMap } from 'rxjs';

export const csrfInterceptor: HttpInterceptorFn = (request, next) => {
  if (!request.url.startsWith('/api/') || ['GET', 'HEAD', 'OPTIONS'].includes(request.method)) return next(request);
  // Obtain a fresh token, including after login/logout changes the session.
  return inject(HttpClient).get<{ headerName: string; token: string }>('/api/auth/csrf').pipe(
    switchMap(csrf => next(request.clone({ setHeaders: { [csrf.headerName]: csrf.token } })))
  );
};
