import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { NotificationService } from './core/notification.service';
import { AuthService } from './core/auth.service';
@Component({ selector: 'app-root', imports: [RouterLink, RouterOutlet], templateUrl: './app.html' })
export class App {
  readonly notifications = inject(NotificationService);
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  logout(): void {
    this.auth.logout().subscribe({
      next: () => { this.notifications.clear(); void this.router.navigate(['/login']); },
      error: () => this.notifications.show('Uscita non riuscita. Riprova.')
    });
  }
}
