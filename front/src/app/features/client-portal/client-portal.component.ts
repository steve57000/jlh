import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { Client } from '../../models/client.model';
import { DemandeSummary } from '../../models/demande.model';
import { Devis } from '../../models/devis.model';

interface MessageState {
  type: 'success' | 'error';
  message: string;
}

@Component({
  selector: 'app-client-portal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './client-portal.component.html',
  styleUrl: './client-portal.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ClientPortalComponent {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(ApiService);

  readonly clientForm = this.fb.nonNullable.group({
    nom: ['', Validators.required],
    prenom: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    telephone: [''],
    adresse: ['']
  });

  readonly demandeForm = this.fb.nonNullable.group({
    clientId: ['', Validators.required],
    typeCode: ['', Validators.required],
    statutCode: ['', Validators.required],
    dateSoumission: [this.nowLocal(), Validators.required]
  });

  readonly devisForm = this.fb.nonNullable.group({
    demandeId: ['', Validators.required],
    dateDevis: [this.nowLocal(), Validators.required],
    montantTotal: ['', Validators.required]
  });

  readonly clientFeedback = signal<MessageState | null>(null);
  readonly demandeFeedback = signal<MessageState | null>(null);
  readonly devisFeedback = signal<MessageState | null>(null);

  readonly latestClient = signal<Client | null>(null);
  readonly latestDemande = signal<DemandeSummary | null>(null);
  readonly latestDevis = signal<Devis | null>(null);

  submitClient(): void {
    if (this.clientForm.invalid) {
      this.clientFeedback.set({ type: 'error', message: 'Veuillez remplir tous les champs obligatoires.' });
      return;
    }
    const payload = this.clientForm.getRawValue();
    this.api.createClient(payload).subscribe({
      next: (client) => {
        this.latestClient.set(client);
        this.clientFeedback.set({ type: 'success', message: 'Compte client créé avec succès.' });
        this.clientForm.reset({ nom: '', prenom: '', email: '', telephone: '', adresse: '' });
      },
      error: () => this.clientFeedback.set({ type: 'error', message: 'Impossible de créer le client. Vérifiez les informations saisies.' })
    });
  }

  submitDemande(): void {
    if (this.demandeForm.invalid) {
      this.demandeFeedback.set({ type: 'error', message: 'Tous les champs sont requis pour la prise de rendez-vous.' });
      return;
    }
    const value = this.demandeForm.getRawValue();
    const payload = {
      dateSoumission: this.toInstant(value.dateSoumission),
      client: { idClient: Number(value.clientId) },
      typeDemande: { codeType: value.typeCode },
      statutDemande: { codeStatut: value.statutCode }
    };
    this.api.createDemande(payload).subscribe({
      next: (demande) => {
        this.latestDemande.set(demande);
        this.demandeFeedback.set({ type: 'success', message: 'Demande enregistrée. Transmise aux équipes JLH.' });
        this.demandeForm.reset({
          clientId: '',
          typeCode: '',
          statutCode: '',
          dateSoumission: this.nowLocal()
        });
      },
      error: () => this.demandeFeedback.set({ type: 'error', message: 'Impossible d’enregistrer la demande. Vérifiez les identifiants fournis.' })
    });
  }

  submitDevis(): void {
    if (this.devisForm.invalid) {
      this.devisFeedback.set({ type: 'error', message: 'Merci de renseigner la demande, la date et le montant estimé.' });
      return;
    }
    const value = this.devisForm.getRawValue();
    const payload = {
      demande: { idDemande: Number(value.demandeId) },
      dateDevis: this.toInstant(value.dateDevis),
      montantTotal: Number(value.montantTotal)
    };
    this.api.createDevis(payload).subscribe({
      next: (devis) => {
        this.latestDevis.set(devis);
        this.devisFeedback.set({ type: 'success', message: 'Votre demande de devis est bien enregistrée.' });
        this.devisForm.reset({
          demandeId: '',
          dateDevis: this.nowLocal(),
          montantTotal: ''
        });
      },
      error: () => this.devisFeedback.set({ type: 'error', message: 'Impossible de générer le devis. Assurez-vous que la demande est valide.' })
    });
  }

  private nowLocal(): string {
    return this.toLocalInput(new Date());
  }

  private toLocalInput(date: Date): string {
    const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return local.toISOString().slice(0, 16);
  }

  private toInstant(value: string): string {
    return new Date(value).toISOString();
  }
}
