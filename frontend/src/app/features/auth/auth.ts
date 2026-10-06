import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/auth.service';

@Component({ selector: 'app-auth', imports: [ReactiveFormsModule], templateUrl: './auth.html' })
export class Auth {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  readonly busy = signal(false);
  readonly error = signal('');
  readonly form = inject(FormBuilder).nonNullable.group({
    user: ['', Validators.required], password: ['', Validators.required]
  });
  login(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.busy()) return;
    this.busy.set(true); this.error.set('');
    this.auth.login(this.form.getRawValue()).pipe(finalize(() => this.busy.set(false))).subscribe({
      next: () => {
        const target = this.route.snapshot.queryParamMap.get('returnUrl');
        void this.router.navigateByUrl(target?.startsWith('/') && !target.startsWith('//') ? target : '/');
      },
      error: err => this.error.set(err.status === 401 ? 'Credenziali non valide.' : 'Accesso non riuscito. Verifica che il backend sia avviato e riprova.')
    });
  }
}

