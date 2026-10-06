export interface AnagraficaRequest {
  nome: string;
  cognome: string;
  codiceFiscale: string;
  email: string;
  telefono: string | null;
  dataNascita: string;
}
export interface Anagrafica extends AnagraficaRequest { id: number; }
export interface ProblemDetail {
  type?: string; title?: string; status?: number; detail?: string; instance?: string;
  errors?: Record<string, string | string[]>;
}
