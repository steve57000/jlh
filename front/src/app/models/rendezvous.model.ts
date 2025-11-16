export interface RendezVous {
  idRdv?: number;
  demande?: { idDemande: number };
  administrateur?: { idAdmin: number };
  creneau?: { idCreneau: number };
  statut?: { codeStatut: string };
}

export type RendezVousPayload = Omit<RendezVous, 'idRdv'>;
