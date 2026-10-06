import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { AnagraficaService } from '../../core/anagrafica.service';
import { NotificationService } from '../../core/notification.service';
import { AnagraficaRequest, ProblemDetail } from '../../models/anagrafica';
function localDate(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}
export function pastDate(control: AbstractControl): ValidationErrors | null {
  const value = control.value as string;
  if (!value) return null;
  const parsed = new Date(`${value}T00:00:00`);
  const valid = /^\d{4}-\d{2}-\d{2}$/.test(value) && !Number.isNaN(parsed.getTime()) && localDate(parsed) === value;
  return valid && value < localDate(new Date()) ? null : { pastDate: true };
}
const notBlank = (control: AbstractControl): ValidationErrors | null => typeof control.value === 'string' && !control.value.trim() ? { required: true } : null;
@Component({ selector: 'app-anagrafica-form', imports: [ReactiveFormsModule, RouterLink], templateUrl: './anagrafica-form.html' })
export class AnagraficaForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(AnagraficaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);
  readonly fields = [
    { key: 'nome', label: 'Nome', type: 'text', autocomplete: 'given-name' },
    { key: 'cognome', label: 'Cognome', type: 'text', autocomplete: 'family-name' },
    { key: 'codiceFiscale', label: 'Codice fiscale', type: 'text', autocomplete: 'off' },
    { key: 'email', label: 'Email', type: 'email', autocomplete: 'email' },
    { key: 'telefono', label: 'Telefono (facoltativo)', type: 'tel', autocomplete: 'tel' },
    { key: 'dataNascita', label: 'Data di nascita', type: 'date', autocomplete: 'bday' }
  ] as const;
  readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, notBlank, Validators.maxLength(50)]],
    cognome: ['', [Validators.required, notBlank, Validators.maxLength(50)]],
    codiceFiscale: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9]{16}$/)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]], telefono: ['', Validators.maxLength(30)],
    dataNascita: ['', [Validators.required, pastDate]]
  });
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly loadError = signal('');
  readonly error = signal('');
  readonly serverErrors = signal<Record<string, string | string[]>>({});
  id: number | null = null;
  ngOnInit(): void {
    this.form.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => { this.serverErrors.set({}); this.error.set(''); });
    const param = this.route.snapshot.paramMap.get('id');
    if (param !== null) {
      this.id = Number(param);
      if (!Number.isSafeInteger(this.id) || this.id <= 0) { this.notFound(); return; }
      this.load();
    }
  }
  load(): void {
    if (this.id === null) return;
    this.loading.set(true); this.loadError.set(''); this.form.disable({ emitEvent: false });
    this.service.findById(this.id).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.loading.set(false))).subscribe({
      next: item => { this.form.patchValue({ ...item, telefono: item.telefono ?? '' }); this.form.enable({ emitEvent: false }); },
      error: (err: HttpErrorResponse) => err.status === 404 ? this.notFound() : this.loadError.set('Impossibile caricare questa anagrafica.')
    });
  }
  fieldError(key: keyof typeof this.form.controls): string {
    const server = this.serverErrors()[key];
    if (server) return Array.isArray(server) ? server.join(' ') : server;
    const control = this.form.controls[key];
    if (!control.touched) return '';
    if (control.hasError('required')) return 'Campo obbligatorio.';
    if (control.hasError('maxlength')) return `Inserisci al massimo ${control.getError('maxlength').requiredLength} caratteri.`;
    if (control.hasError('pattern')) return 'Inserisci 16 caratteri alfanumerici.';
    if (control.hasError('email')) return 'Inserisci un indirizzo email valido.';
    if (control.hasError('pastDate')) return 'Inserisci una data valida precedente a oggi.';
    return '';
  }
  save(): void {
    if (this.loading() || this.saving() || this.loadError()) return;
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    const value = this.form.getRawValue();
    const body: AnagraficaRequest = { ...value, nome: value.nome.trim(), cognome: value.cognome.trim(), codiceFiscale: value.codiceFiscale.toUpperCase(), email: value.email.trim(), telefono: value.telefono.trim() || null };
    this.saving.set(true); this.error.set(''); this.serverErrors.set({}); this.form.disable({ emitEvent: false });
    const request = this.id === null ? this.service.create(body) : this.service.update(this.id, body);
    request.pipe(takeUntilDestroyed(this.destroyRef), finalize(() => { this.saving.set(false); this.form.enable({ emitEvent: false }); })).subscribe({
      next: () => { this.notifications.show('Anagrafica salvata.'); void this.router.navigate(['/']); },
      error: (err: HttpErrorResponse) => {
        const problem = err.error as ProblemDetail | null;
        if (err.status === 404) { this.notFound(); return; }
        if (err.status === 409) this.error.set("Esiste già un'anagrafica con questo codice fiscale");
        else if (err.status === 400) { this.serverErrors.set(problem?.errors ?? {}); this.error.set(problem?.detail ?? 'Controlla i dati inseriti.'); }
        else this.error.set('Salvataggio non riuscito. Riprova.');
      }
    });
  }
  private notFound(): void { this.notifications.show('Anagrafica inesistente.'); void this.router.navigate(['/']); }
}
