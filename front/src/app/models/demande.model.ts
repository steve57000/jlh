export interface DemandeSummary {
  idDemande?: number;
  dateSoumission: string;
  client?: { idClient: number; nom?: string; prenom?: string };
  typeDemande?: { codeType: string; libelle?: string };
  statutDemande?: { codeStatut: string; libelle?: string };
}

export type DemandePayload = Omit<DemandeSummary, 'idDemande'>;
