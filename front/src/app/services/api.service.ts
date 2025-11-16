import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Client, ClientPayload } from '../models/client.model';
import { DemandePayload, DemandeSummary } from '../models/demande.model';
import { Devis, DevisPayload } from '../models/devis.model';
import { RendezVous, RendezVousPayload } from '../models/rendezvous.model';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api';

  createClient(payload: ClientPayload): Observable<Client> {
    return this.http.post<Client>(`${this.baseUrl}/clients`, payload);
  }

  getClients(): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.baseUrl}/clients`);
  }

  createDemande(payload: DemandePayload): Observable<DemandeSummary> {
    return this.http.post<DemandeSummary>(`${this.baseUrl}/demandes`, payload);
  }

  updateDemande(id: number, payload: DemandePayload): Observable<DemandeSummary> {
    return this.http.put<DemandeSummary>(`${this.baseUrl}/demandes/${id}`, payload);
  }

  getDemandes(): Observable<DemandeSummary[]> {
    return this.http.get<DemandeSummary[]>(`${this.baseUrl}/demandes`);
  }

  createDevis(payload: DevisPayload): Observable<Devis> {
    return this.http.post<Devis>(`${this.baseUrl}/devis`, payload);
  }

  updateDevis(id: number, payload: DevisPayload): Observable<Devis> {
    return this.http.put<Devis>(`${this.baseUrl}/devis/${id}`, payload);
  }

  getDevis(): Observable<Devis[]> {
    return this.http.get<Devis[]>(`${this.baseUrl}/devis`);
  }

  createRendezVous(payload: RendezVousPayload): Observable<RendezVous> {
    return this.http.post<RendezVous>(`${this.baseUrl}/rendezvous`, payload);
  }

  getRendezVous(): Observable<RendezVous[]> {
    return this.http.get<RendezVous[]>(`${this.baseUrl}/rendezvous`);
  }
}
