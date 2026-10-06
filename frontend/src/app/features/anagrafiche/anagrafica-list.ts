import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { AnagraficaService } from '../../core/anagrafica.service';
import { NotificationService } from '../../core/notification.service';
import { Anagrafica } from '../../models/anagrafica';

@Component({ selector: 'app-home', imports: [RouterLink, DatePipe], templateUrl: './anagrafica-list.html' })
export class AnagraficaList implements OnInit {
  private readonly service = inject(AnagraficaService);
  private readonly notifications = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);
  readonly items = signal<Anagrafica[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly deletingId = signal<number | null>(null);
  ngOnInit(): void { this.load(); }
  load(): void {
    this.loading.set(true); this.error.set('');
    this.service.findAll().pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.loading.set(false))).subscribe({
      next: items => this.items.set([...items].sort((a, b) => a.cognome.localeCompare(b.cognome, 'it') || a.nome.localeCompare(b.nome, 'it'))),
      error: () => this.error.set('Impossibile caricare le anagrafiche.')
    });
  }
  remove(item: Anagrafica): void {
    if (this.deletingId() !== null || !confirm(`Eliminare ${item.nome} ${item.cognome}?`)) return;
    this.deletingId.set(item.id);
    this.service.delete(item.id).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.deletingId.set(null))).subscribe({
      next: () => { this.items.update(items => items.filter(a => a.id !== item.id)); this.notifications.show('Anagrafica eliminata.'); },
      error: error => {
        if (error.status === 404) { this.notifications.show('Anagrafica non più presente.'); this.load(); }
        else this.error.set('Eliminazione non riuscita. Puoi riprovare dalla riga.');
      }
    });
  }
}
