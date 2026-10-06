import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Anagrafica, AnagraficaRequest } from '../models/anagrafica';

@Injectable({ providedIn: 'root' })
export class AnagraficaService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/anagrafiche';
  findAll(): Observable<Anagrafica[]> { return this.http.get<Anagrafica[]>(this.url); }
  findById(id: number): Observable<Anagrafica> { return this.http.get<Anagrafica>(`${this.url}/${id}`); }
  create(body: AnagraficaRequest): Observable<Anagrafica> { return this.http.post<Anagrafica>(this.url, body); }
  update(id: number, body: AnagraficaRequest): Observable<Anagrafica> { return this.http.put<Anagrafica>(`${this.url}/${id}`, body); }
  delete(id: number): Observable<void> { return this.http.delete<void>(`${this.url}/${id}`); }
}
