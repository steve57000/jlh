import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ApiService } from '../../services/api.service';
import { DemandeSummary } from '../../models/demande.model';
import { Devis } from '../../models/devis.model';
import { RendezVous } from '../../models/rendezvous.model';
import { Client } from '../../models/client.model';

interface MessageState {
  type: 'success' | 'error';
  message: string;
}

@Component({
  selector: 'app-admin-console',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-console.component.html',
  styleUrl: './admin-console.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminConsoleComponent {
  private readonly api = inject(ApiService);
  private readonly fb = inject(FormBuilder);

  readonly demandes = signal<DemandeSummary[]>([]);
  readonly devis = signal<Devis[]>([]);
  readonly rendezVous = signal<RendezVous[]>([]);
  readonly clients = signal<Client[]>([]);

  readonly status = signal<MessageState | null>(null);

  readonly rendezVousForm = this.fb.nonNullable.group({
    demandeId: ['', Validators.required],
    adminId: ['', Validators.required],
    creneauId: ['', Validators.required],
    statutCode: ['', Validators.required]
  });

  readonly demandeUpdateForm = this.fb.nonNullable.group({
    demandeId: ['', Validators.required],
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

  readonly devisUpdateForm = this.fb.nonNullable.group({
    devisId: ['', Validators.required],
    demandeId: ['', Validators.required],
    dateDevis: [this.nowLocal(), Validators.required],
    montantTotal: ['', Validators.required]
  });

  constructor() {
    this.loadAll();
  }

  submitRendezVous(): void {
    if (this.rendezVousForm.invalid) {
      this.status.set({ type: 'error', message: 'Tous les champs sont requis pour créer un rendez-vous.' });
      return;
    }
    const value = this.rendezVousForm.getRawValue();
    const payload = {
      demande: { idDemande: Number(value.demandeId) },
      administrateur: { idAdmin: Number(value.adminId) },
      creneau: { idCreneau: Number(value.creneauId) },
      statut: { codeStatut: value.statutCode }
    };
    this.api.createRendezVous(payload).subscribe({
      next: () => {
        this.status.set({ type: 'success', message: 'Rendez-vous planifié.' });
        this.rendezVousForm.reset({ demandeId: '', adminId: '', creneauId: '', statutCode: '' });
        this.refreshRendezVous();
      },
      error: () => this.status.set({ type: 'error', message: 'Impossible de créer le rendez-vous (contrôle des contraintes).' })
    });
  }

  submitDemandeUpdate(): void {
    if (this.demandeUpdateForm.invalid) {
      this.status.set({ type: 'error', message: 'Tous les champs sont requis pour modifier une demande.' });
      return;
    }
    const value = this.demandeUpdateForm.getRawValue();
    const payload = {
      dateSoumission: this.toInstant(value.dateSoumission),
      client: { idClient: Number(value.clientId) },
      typeDemande: { codeType: value.typeCode },
      statutDemande: { codeStatut: value.statutCode }
    };
    this.api.updateDemande(Number(value.demandeId), payload).subscribe({
      next: () => {
        this.status.set({ type: 'success', message: 'Demande mise à jour.' });
        this.refreshDemandes();
      },
      error: () => this.status.set({ type: 'error', message: 'Mise à jour impossible. Vérifiez les identifiants fournis.' })
    });
  }

  submitDevisCreation(): void {
    if (this.devisForm.invalid) {
      this.status.set({ type: 'error', message: 'Merci de renseigner la demande cible, la date et le montant.' });
      return;
    }
    const value = this.devisForm.getRawValue();
    const payload = {
      demande: { idDemande: Number(value.demandeId) },
      dateDevis: this.toInstant(value.dateDevis),
      montantTotal: Number(value.montantTotal)
    };
    this.api.createDevis(payload).subscribe({
      next: () => {
        this.status.set({ type: 'success', message: 'Devis ajouté au dossier.' });
        this.devisForm.reset({ demandeId: '', dateDevis: this.nowLocal(), montantTotal: '' });
        this.refreshDevis();
      },
      error: () => this.status.set({ type: 'error', message: 'Impossible de générer le devis (demande inexistante ou déjà liée).' })
    });
  }

  submitDevisUpdate(): void {
    if (this.devisUpdateForm.invalid) {
      this.status.set({ type: 'error', message: 'Tous les champs sont requis pour modifier un devis.' });
      return;
    }
    const value = this.devisUpdateForm.getRawValue();
    const payload = {
      demande: { idDemande: Number(value.demandeId) },
      dateDevis: this.toInstant(value.dateDevis),
      montantTotal: Number(value.montantTotal)
    };
    this.api.updateDevis(Number(value.devisId), payload).subscribe({
      next: () => {
        this.status.set({ type: 'success', message: 'Devis mis à jour.' });
        this.refreshDevis();
      },
      error: () => this.status.set({ type: 'error', message: 'Mise à jour du devis impossible.' })
    });
  }

  private loadAll(): void {
    this.refreshDemandes();
    this.refreshDevis();
    this.refreshRendezVous();
    this.api.getClients().pipe(takeUntilDestroyed()).subscribe({
      next: (clients) => this.clients.set(clients),
      error: () => this.status.set({ type: 'error', message: 'Impossible de récupérer la liste des clients.' })
    });
  }

  private refreshDemandes(): void {
    this.api.getDemandes().pipe(takeUntilDestroyed()).subscribe({
      next: (data) => this.demandes.set(data),
      error: () => this.status.set({ type: 'error', message: 'Erreur lors du chargement des demandes.' })
    });
  }

  private refreshDevis(): void {
    this.api.getDevis().pipe(takeUntilDestroyed()).subscribe({
      next: (data) => this.devis.set(data),
      error: () => this.status.set({ type: 'error', message: 'Erreur lors du chargement des devis.' })
    });
  }

  private refreshRendezVous(): void {
    this.api.getRendezVous().pipe(takeUntilDestroyed()).subscribe({
      next: (data) => this.rendezVous.set(data),
      error: () => this.status.set({ type: 'error', message: 'Erreur lors du chargement des rendez-vous.' })
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
