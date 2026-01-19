import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { environment } from '../../environments/environment';
import { AuthService } from '../services/auth.service';
import { DemandesStateService } from '../services/demandes-state.service';
import { DemandesServiceService } from '../services/demandes-services.service';
import { ToastService } from '../shared/toast/toast.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './contact.component.html',
  styleUrl: './contact.component.scss'
})
export class ContactComponent {
  rdvTelephone = '';
  rdvImmatriculation = '';
  rdvDescription = '';
  submitting = false;

  constructor(
    private auth: AuthService,
    private state: DemandesStateService,
    private demandesService: DemandesServiceService,
    private http: HttpClient,
    private toast: ToastService
  ) {}

  get isClient(): boolean {
    return this.auth.isAuthenticated() && this.auth.getUserRole() === 'CLIENT';
  }

  async submitRendezVousRequest() {
    if (!this.isClient || this.submitting) {
      return;
    }
    const description = this.rdvDescription.trim();
    if (!description) {
      this.toast.error('Description requise', 'Merci de préciser la raison du rendez-vous.');
      return;
    }
    this.submitting = true;
    const api = environment.apiBaseUrl;
    const skipErrorOptions = { headers: new HttpHeaders({ 'X-Skip-Error-Toast': '1' }) };
    try {
      const idDemande = await this.state.initDemande({ silent: true });
      const immat = this.rdvImmatriculation.trim();
      const telephone = this.rdvTelephone.trim();

      const fallback: { codeType?: 'RendezVous'; immatriculation?: string | null; telephone?: string | null } = {};

      try {
        await firstValueFrom(
          this.http.patch<void>(
            `${api}/demandes/${idDemande}/type`,
            { codeType: 'RendezVous' },
            skipErrorOptions
          )
        );
      } catch {
        fallback.codeType = 'RendezVous';
      }

      const clientPatch: { immatriculation?: string | null; telephone?: string | null } = {};
      if (immat) {
        clientPatch.immatriculation = immat;
      }
      if (telephone) {
        clientPatch.telephone = telephone;
      }
      if (Object.keys(clientPatch).length) {
        try {
          await firstValueFrom(
            this.http.patch<void>(
              `${api}/demandes/${idDemande}/client`,
              clientPatch,
              skipErrorOptions
            )
          );
        } catch {
          fallback.immatriculation = immat || null;
          fallback.telephone = telephone || null;
        }
      }

      if (fallback.codeType || 'immatriculation' in fallback || 'telephone' in fallback) {
        await firstValueFrom(
          this.demandesService.updateDemande(
            idDemande,
            {
              ...(fallback.codeType ? { codeType: fallback.codeType } : {}),
              ...('immatriculation' in fallback ? { immatriculation: fallback.immatriculation ?? null } : {}),
              ...('telephone' in fallback ? { telephone: fallback.telephone ?? null } : {})
            },
            { silentError: true }
          )
        );
      }

      await firstValueFrom(
        this.http.post(
          `${api}/demandes/${idDemande}/timeline/commentaire`,
          { commentaire: description },
          skipErrorOptions
        )
      );

      await firstValueFrom(
        this.http.patch<void>(
          `${api}/demandes/${idDemande}/submit`,
          {},
          skipErrorOptions
        )
      );

      this.toast.success('Demande envoyée', 'Votre demande de rendez-vous a bien été transmise.');
      this.rdvDescription = '';
    } catch (err: any) {
      const msg = err?.error?.message || err?.message || 'Envoi impossible';
      this.toast.error('Échec de la demande', msg);
    } finally {
      this.submitting = false;
    }
  }
}
