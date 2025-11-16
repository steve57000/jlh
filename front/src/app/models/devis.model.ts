export interface Devis {
  idDevis?: number;
  dateDevis: string;
  montantTotal: number;
  demande?: { idDemande: number };
}

export type DevisPayload = Omit<Devis, 'idDevis'>;
