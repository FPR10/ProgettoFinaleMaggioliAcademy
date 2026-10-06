import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, map, of, tap, throwError } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  readonly user = signal<string | null>(null);
  checkSession() {
    return this.http.get<{ user: string }>('/api/auth/me').pipe(
      tap(result => this.user.set(result.user)), map(() => true),
      catchError((err: HttpErrorResponse) => {
        this.user.set(null);
        return err.status === 401 ? of(false) : throwError(() => err);
      })
    );
  }
  login(body: { user: string; password: string }) {
    return this.http.post<{ user: string }>('/api/auth/login', body).pipe(tap(result => this.user.set(result.user)));
  }
  logout() {
    return this.http.post<void>('/api/auth/logout', {}).pipe(tap(() => this.user.set(null)));
  }
}
