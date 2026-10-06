# Rubrica anagrafiche - frontend Angular 22

Frontend standalone collegato al backend Spring Boot presente in `../../backend`. Comprende login, logout e flussi CRUD con sessione e protezione CSRF. Per l'avvio completo vedi il README nella cartella principale del progetto.

## Avvio

Usa Node 24.19 (vedi `.nvmrc`) o una versione supportata da Angular 22: https://angular.dev/reference/versions.

```sh
npm ci
npm start
```

Apri http://localhost:4200 e accedi con le credenziali demo `admin` / `admin`. Avvia Spring Boot su http://localhost:8080: il proxy inoltra `/api/**` al backend. Senza backend il login mostra un errore. Non sono inclusi dati mock.

```sh
npm run build
```

Il proxy vale per il server di sviluppo. In produzione configura il reverse proxy `/api` sul server di hosting.

## Struttura

- `src/app/app.ts` e `app.html`: intestazione, router outlet, area notifiche.
- `src/app/app.routes.ts`: home, nuova, modifica, fallback. Componenti in lazy loading.
- `src/app/models/anagrafica.ts`: Anagrafica, AnagraficaRequest, ProblemDetail.
- `src/app/core/anagrafica.service.ts`: un metodo HttpClient/Observable per endpoint.
- `src/app/core/error.interceptor.ts`: notifiche per status 0 e 5xx, propagazione al componente.
- `src/app/features/anagrafiche/anagrafica-list.*`: elenco ordinato, caricamento, errore/Riprova, lista vuota, conferma eliminazione e aggiornamento locale.
- `src/app/features/anagrafiche/anagrafica-form.*`: stesso Reactive Form per POST e PUT, caricamento dati, validazione e gestione errori.
- `src/styles.scss`: stile responsive essenziale.

## Contratto API

| Metodo | URL | Risposta |
| --- | --- | --- |
| GET | `/api/anagrafiche` | Anagrafica[] |
| GET | `/api/anagrafiche/{id}` | Anagrafica |
| POST | `/api/anagrafiche` | Anagrafica (201) |
| PUT | `/api/anagrafiche/{id}` | Anagrafica (200) |
| DELETE | `/api/anagrafiche/{id}` | Nessun body (204) |

POST/PUT inviano AnagraficaRequest senza id. Date `yyyy-MM-dd` nel JSON, `dd/MM/yyyy` nella tabella. Telefono vuoto inviato come null. Codice fiscale convertito in maiuscolo prima del salvataggio.

## Validazione ed errori

Nome e cognome obbligatori, massimo 50 caratteri (spazi soli non ammessi). Codice fiscale obbligatorio, 16 caratteri alfanumerici. Email obbligatoria e valida. Nascita obbligatoria, data valida precedente a oggi. Telefono facoltativo. Il backend deve ripetere la validazione e verificare l'unicità del codice fiscale.

- 400: messaggio del form e `ProblemDetail.errors` sotto i campi (stringa o array di stringhe).
- 409: messaggio dedicato al codice fiscale duplicato.
- 404 durante caricamento o salvataggio: notifica e ritorno alla home.
- 404 durante eliminazione: notifica e ricaricamento elenco.
- 0 e 5xx: notifica dall'interceptor.

## Scelte

Reactive Forms, esplicitamente ammessi dalla traccia, per una base semplice da estendere. Signals per lo stato, Observable per HTTP, servizi iniettati con `inject` e `takeUntilDestroyed` per il ciclo di vita. Le estensioni facoltative ricerca e paginazione non sono incluse. Gli errori di campo dal server sono già supportati.

## Verifica manuale con backend

1. Elenco ordinato per cognome e nome, date italiane.
2. Salva con campi vuoti: errori e nessuna chiamata HTTP.
3. Inserimento valido: ritorno alla home con nuovo contatto.
4. Codice fiscale duplicato: messaggio 409.
5. Modifica precompilata, cambio email persistito.
6. Elimina: Annulla mantiene la riga, conferma la rimuove.
7. `/anagrafiche/99/modifica` inesistente: notifica e home.
8. Backend spento: notifica e Riprova nell'elenco.

Il backend include `ApiIntegrationTests`, che verifica il flusso HTTP con sessione, CSRF e CRUD. Per verificare anche il proxy locale avvia backend e frontend insieme, oppure usa `npm run build` e `npm run preview`.

Verifica eseguita: controllo Angular di TypeScript e template e build di produzione riusciti con Node 24.19. In questo ambiente Windows la build ha usato `NG_BUILD_SASS_EMBEDDED=false` e `NG_BUILD_MAX_WORKERS=1`. Se il compilatore Sass nativo dà problemi anche sul tuo PC, in PowerShell:

```powershell
$env:NG_BUILD_SASS_EMBEDDED='false'
$env:NG_BUILD_MAX_WORKERS='1'
npm run build
```
