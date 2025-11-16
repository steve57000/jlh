export interface Client {
  idClient?: number;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  adresse?: string;
}

export type ClientPayload = Omit<Client, 'idClient'>;
