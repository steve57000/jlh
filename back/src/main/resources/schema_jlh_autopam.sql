-- Table des clients
CREATE TABLE Client (
  id_client      INT AUTO_INCREMENT PRIMARY KEY,
  nom            VARCHAR(100) NOT NULL,
  prenom         VARCHAR(100) NOT NULL,
  email          VARCHAR(150) UNIQUE NOT NULL,
  telephone      VARCHAR(20),
  adresse        TEXT
);

-- Table des services
CREATE TABLE Service (
  id_service     INT AUTO_INCREMENT PRIMARY KEY,
  libelle        VARCHAR(100) NOT NULL,
  description    TEXT,
  prix_unitaire  DECIMAL(10,2) NOT NULL,
  quantite_max   INT NOT NULL DEFAULT 1,
  archived       TINYINT(1) NOT NULL DEFAULT 0
);

-- Lookup table : types de demande
CREATE TABLE Type_Demande (
  code_type      VARCHAR(20) PRIMARY KEY,
  libelle        VARCHAR(100) NOT NULL
);
INSERT INTO Type_Demande VALUES
  ('Devis',      'Devis'),
  ('RendezVous', 'Rendez-vous');

-- Lookup table : statuts de demande
CREATE TABLE Statut_Demande (
  code_statut    VARCHAR(20) PRIMARY KEY,
  libelle        VARCHAR(100) NOT NULL
);
INSERT INTO Statut_Demande VALUES
  ('En_attente', 'En attente'),
  ('Traitee',    'Traitée'),
  ('Annulee',    'Annulée');

-- Table des demandes
CREATE TABLE Demande (
  id_demande      INT AUTO_INCREMENT PRIMARY KEY,
  id_client       INT NOT NULL,
  date_soumission DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  code_type       VARCHAR(20) NOT NULL,
  code_statut     VARCHAR(20) NOT NULL DEFAULT 'En_attente',
  FOREIGN KEY (id_client)   REFERENCES Client(id_client),
  FOREIGN KEY (code_type)   REFERENCES Type_Demande(code_type),
  FOREIGN KEY (code_statut) REFERENCES Statut_Demande(code_statut)
);

-- Table des documents attachés aux demandes
CREATE TABLE Demande_Document (
  id_document   BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_demande    INT NOT NULL,
  filename      VARCHAR(255) NOT NULL,
  content_type  VARCHAR(150) NOT NULL,
  file_size     BIGINT NOT NULL,
  data          LONGBLOB NOT NULL,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_demande) REFERENCES Demande(id_demande) ON DELETE CASCADE
);

-- Table de jointure Demande ↔ Service
CREATE TABLE Demande_Service (
  id_demande      INT NOT NULL,
  id_service      INT NOT NULL,
  quantite        INT NOT NULL DEFAULT 1,
  PRIMARY KEY (id_demande, id_service),
  FOREIGN KEY (id_demande)  REFERENCES Demande(id_demande),
  FOREIGN KEY (id_service)  REFERENCES Service(id_service)
);

-- Table des devis
CREATE TABLE Devis (
  id_devis        INT AUTO_INCREMENT PRIMARY KEY,
  id_demande      INT UNIQUE NOT NULL,
  date_devis      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  montant_total   DECIMAL(12,2) NOT NULL,
  FOREIGN KEY (id_demande) REFERENCES Demande(id_demande)
);

-- Lookup table : statuts de créneau
CREATE TABLE Statut_Creneau (
  code_statut    VARCHAR(20) PRIMARY KEY,
  libelle        VARCHAR(100) NOT NULL
);
INSERT INTO Statut_Creneau VALUES
  ('Libre',        'Libre'),
  ('Reserve',      'Réservé'),
  ('Indisponible', 'Indisponible');

-- Table des créneaux
CREATE TABLE Creneau (
  id_creneau     INT AUTO_INCREMENT PRIMARY KEY,
  date_debut     DATETIME NOT NULL,
  date_fin       DATETIME NOT NULL,
  code_statut    VARCHAR(20) NOT NULL DEFAULT 'Libre',
  FOREIGN KEY (code_statut) REFERENCES Statut_Creneau(code_statut)
);

-- Table des administrateurs
CREATE TABLE Administrateur (
  id_admin       INT AUTO_INCREMENT PRIMARY KEY,
  username       VARCHAR(50) UNIQUE NOT NULL,
  mot_de_passe   VARCHAR(255) NOT NULL,
  nom            VARCHAR(100),
  prenom         VARCHAR(100),
  email          VARCHAR(150) UNIQUE
);

-- Table des disponibilités (planning admin)
CREATE TABLE Disponibilite (
  id_admin       INT NOT NULL,
  id_creneau     INT NOT NULL,
  PRIMARY KEY (id_admin, id_creneau),
  FOREIGN KEY (id_admin)   REFERENCES Administrateur(id_admin),
  FOREIGN KEY (id_creneau) REFERENCES Creneau(id_creneau)
);

-- Lookup table : statuts de rendez-vous
CREATE TABLE Statut_RendezVous (
  code_statut    VARCHAR(20) PRIMARY KEY,
  libelle        VARCHAR(100) NOT NULL
);
INSERT INTO Statut_RendezVous VALUES
  ('Confirme', 'Confirmé'),
  ('Reporte',  'Reporté'),
  ('Annule',   'Annulé');

-- Table des rendez-vous
CREATE TABLE RendezVous (
  id_rdv         INT AUTO_INCREMENT PRIMARY KEY,
  id_demande     INT UNIQUE NOT NULL,
  id_admin       INT NOT NULL,
  id_creneau     INT UNIQUE NOT NULL,
  code_statut    VARCHAR(20) NOT NULL DEFAULT 'Confirme',
  FOREIGN KEY (id_demande)  REFERENCES Demande(id_demande),
  FOREIGN KEY (id_admin)    REFERENCES Administrateur(id_admin),
  FOREIGN KEY (id_creneau)  REFERENCES Creneau(id_creneau),
  FOREIGN KEY (code_statut) REFERENCES Statut_RendezVous(code_statut)
);

-- Table des documents associés aux demandes
CREATE TABLE demande_document (
  id_document      BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_demande       INT NOT NULL,
  nom_fichier      VARCHAR(255) NOT NULL,
  url_public       VARCHAR(512),
  type_contenu     VARCHAR(100),
  taille_octets    BIGINT,
  visible_client   TINYINT(1) NOT NULL DEFAULT 1,
  cree_par         VARCHAR(150),
  cree_par_role    VARCHAR(30),
  cree_le          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_demande) REFERENCES Demande(id_demande)
);

-- Timeline des demandes (statuts, commentaires, montants, rendez-vous...)
CREATE TABLE demande_timeline (
  id_timeline                  BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_demande                   INT NOT NULL,
  type_evenement               VARCHAR(30) NOT NULL,
  cree_le                      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  cree_par                     VARCHAR(150),
  cree_par_role                VARCHAR(30),
  visible_client               TINYINT(1) NOT NULL DEFAULT 1,
  statut_code                  VARCHAR(20),
  statut_libelle               VARCHAR(100),
  commentaire                  TEXT,
  montant_valide               DECIMAL(12,2),
  document_id                  BIGINT,
  document_nom                 VARCHAR(255),
  document_url                 VARCHAR(512),
  rendezvous_id                INT,
  rendezvous_statut_code       VARCHAR(20),
  rendezvous_statut_libelle    VARCHAR(100),
  rendezvous_date_debut        DATETIME,
  rendezvous_date_fin          DATETIME,
  FOREIGN KEY (id_demande) REFERENCES Demande(id_demande)
);
