-- ==================================================
-- 0) Sécurité : colonnes de vérification e-mail (MySQL-safe)
-- ==================================================
SET @db := DATABASE();

-- Ajout colonne email_verified (bool = tinyint(1))
SET @exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'client' AND COLUMN_NAME = 'email_verified'
);
SET @ddl := IF(@exists = 0,
               'ALTER TABLE client ADD COLUMN email_verified TINYINT(1) NOT NULL DEFAULT 0',
               'SELECT 1'
            );
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Ajout colonne email_verified_at (sans time zone en MySQL)
SET @exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'client' AND COLUMN_NAME = 'email_verified_at'
);
SET @ddl := IF(@exists = 0,
               'ALTER TABLE client ADD COLUMN email_verified_at DATETIME NULL',
               'SELECT 1'
            );
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ==================================================
-- 1) Lookups
-- ==================================================
INSERT INTO type_demande (code_type, libelle) VALUES
                                                  ('Devis', 'Devis'),
                                                  ('Service', 'Service'),
                                                  ('RendezVous', 'Rendez-vous');

INSERT INTO statut_demande (code_statut, libelle) VALUES
                                                      ('Brouillon', 'Brouillon'),
                                                      ('En_attente', 'En attente'),
                                                      ('Traitee',    'Traitée'),
                                                      ('Annulee',    'Annulée');

INSERT INTO statut_creneau (code_statut, libelle) VALUES
                                                      ('Libre',        'Libre'),
                                                      ('Reserve',      'Réservé'),
                                                      ('Indisponible', 'Indisponible');

INSERT INTO statut_rendez_vous (code_statut, libelle) VALUES
                                                          ('Confirme', 'Confirmé'),
                                                          ('Reporte',  'Reporté'),
                                                          ('Annule',   'Annulé');

-- ==================================================
-- 2) Services
-- ==================================================
INSERT INTO service (id_service, libelle, description, prix_unitaire, archived) VALUES
                                                                          (1, 'Vidange',
                                                                           'Vidange moteur complète avec huile synthétique haute performance et remplacement du filtre à huile pour optimiser la longévité de votre moteur',
                                                                           59.90, 0),
                                                                          (2, 'Révision',
                                                                           'Révision générale incluant le contrôle et le remplacement des courroies, filtres (air, habitacle, carburant) et bougies, ainsi que la vérification des niveaux de liquide',
                                                                           129.90, 0),
                                                                          (3, 'Freinage',
                                                                           'Remplacement des plaquettes de frein avant par des plaquettes haute performance, contrôle des disques et purge complète du circuit de freinage pour une sécurité maximale',
                                                                           199.00, 0),
                                                                          (4, 'Pneumatiques',
                                                                           'Montage et équilibrage de quatre pneus toutes saisons, vérification de la géométrie et conseil personnalisé pour un confort et une adhérence optimaux',
                                                                           449.00, 0),
                                                                          (5, 'Diagnostic',
                                                                           'Diagnostic électronique multimarque complet avec intervention valise électronique, analyse des défauts et remise d’un rapport détaillé',
                                                                           79.00, 0);

-- ==================================================
-- 3) Clients (mots de passe déjà hashés)
--    On renseigne explicitement email_verified / email_verified_at
-- ==================================================
INSERT INTO client (
    id_client, nom, prenom, email, telephone,
    adresse_ligne1, adresse_ligne2, adresse_code_postal, adresse_ville,
    immatriculation, mot_de_passe,
    email_verified, email_verified_at
) VALUES
      (1,'Durand','Alice','test@client1.fr','0601020304',
       '12 rue Victor Hugo', NULL, '75003', 'Paris',
       'AA-123-AA',
       '$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW',
       1, '2025-06-01 10:00:00'),

      (2,'Martin','Bob','test@client2.fr','0605060708',
       '45 av. Jean Jaurès', NULL, '69007', 'Lyon',
       'BB-234-BB',
       '$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW',
       1, '2025-06-01 10:00:00'),

      (3,'Bernard','Claire','test@client3.fr','0611121314',
       '78 bd Haussmann', NULL, '75009', 'Paris',
       'CC-345-CC',
       '$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW',
       1, '2025-06-01 10:00:00'),

      (4,'Lefevre','David','test@client4.fr','0622232425',
       '3 place Bellecour', NULL, '69002', 'Lyon',
       'DD-456-DD',
       '$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW',
       1, '2025-06-01 10:00:00'),

      (5,'Dupont','Eva','test@client5.fr','0633343536',
       '6 quai de la Loire', NULL, '44000', 'Nantes',
       'EE-567-EE',
       '$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW',
       0, NULL);


-- ==================================================
-- 4) Admins
-- ==================================================
INSERT INTO administrateur (id_admin, email, mot_de_passe, nom, prenom) VALUES
    (1,'test@admin.fr','$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW','Bongeot','Michael');

-- ==================================================
-- 5) Créneaux
-- ==================================================
INSERT INTO creneau (id_creneau, date_debut, date_fin, code_statut) VALUES
                                                                        (1,'2025-07-01 09:00:00','2025-07-01 10:00:00','Reserve'),   -- utilisé par RDV#1 (Confirmé)
                                                                        (2,'2025-07-01 10:00:00','2025-07-01 11:00:00','Libre'),
                                                                        (3,'2025-07-01 11:00:00','2025-07-01 12:00:00','Reserve'),   -- utilisé par RDV#2 (Reporté)
                                                                        (4,'2025-07-01 14:00:00','2025-07-01 15:00:00','Indisponible'),
                                                                        (5,'2025-07-02 09:00:00','2025-07-02 10:00:00','Libre'),
                                                                        (6,'2025-07-02 10:00:00','2025-07-02 11:00:00','Reserve');   -- utilisé par RDV#3 (Annulé)

-- ==================================================
-- 6) Disponibilités
-- ==================================================
INSERT INTO disponibilite (id_admin, id_creneau) VALUES
                                                     (1,1),(1,2),(1,3),(1,5),(1,6);

-- ==================================================
-- 7) Demandes
-- ==================================================
-- Devis: Alice (En attente), multiple services
INSERT INTO demande (id_demande, id_client, date_demande, code_type, code_statut) VALUES
    (1, 1, '2025-06-20 08:15:00', 'Devis',     'En_attente');

-- Devis: Bob (Traitée)
INSERT INTO demande (id_demande, id_client, date_demande, code_type, code_statut) VALUES
    (2, 2, '2025-06-19 09:30:00', 'Devis',     'Traitee');

-- RDV: Claire (Confirmé ensuite via RDV)
INSERT INTO demande (id_demande, id_client, date_demande, code_type, code_statut) VALUES
    (3, 3, '2025-06-18 10:45:00', 'RendezVous','En_attente');

-- RDV: David (Reporté ensuite via RDV)
INSERT INTO demande (id_demande, id_client, date_demande, code_type, code_statut) VALUES
    (4, 4, '2025-06-17 11:00:00', 'RendezVous','Traitee');

-- Devis: Eva (Annulée)
INSERT INTO demande (id_demande, id_client, date_demande, code_type, code_statut) VALUES
    (5, 5, '2025-06-16 12:00:00', 'Devis',     'Annulee');

-- RDV: Alice (Annulé ensuite via RDV)
INSERT INTO demande (id_demande, id_client, date_demande, code_type, code_statut) VALUES
    (6, 1, '2025-06-15 13:00:00', 'RendezVous','Annulee');

-- ==================================================
-- 8) Demande_Service
-- ==================================================
-- Demande 1 : Alice veut Vidange + Diagnostic
INSERT INTO demande_service (
    id_demande, id_service, quantite,
    libelle_service, description_service, prix_unitaire_service
) VALUES
    (1, 1, 1,
     'Vidange',
     'Vidange moteur complète avec huile synthétique haute performance et remplacement du filtre à huile pour optimiser la longévité de votre moteur',
     59.90),
    (1, 5, 1,
     'Diagnostic',
     'Diagnostic électronique multimarque complet avec intervention valise électronique, analyse des défauts et remise d’un rapport détaillé',
     79.00);

-- Demande 2 : Bob veut Révision
INSERT INTO demande_service (
    id_demande, id_service, quantite,
    libelle_service, description_service, prix_unitaire_service
) VALUES
    (2, 2, 1,
     'Révision',
     'Révision générale incluant le contrôle et le remplacement des courroies, filtres (air, habitacle, carburant) et bougies, ainsi que la vérification des niveaux de liquide',
     129.90);

-- Demande 3 : Claire veut Changement pneus x4
INSERT INTO demande_service (
    id_demande, id_service, quantite,
    libelle_service, description_service, prix_unitaire_service
) VALUES
    (3, 4, 1,
     'Pneumatiques',
     'Montage et équilibrage de quatre pneus toutes saisons, vérification de la géométrie et conseil personnalisé pour un confort et une adhérence optimaux',
     449.00);

-- Demande 5 : Eva voulait Diagnostic
INSERT INTO demande_service (
    id_demande, id_service, quantite,
    libelle_service, description_service, prix_unitaire_service
) VALUES
    (5, 5, 1,
     'Diagnostic',
     'Diagnostic électronique multimarque complet avec intervention valise électronique, analyse des défauts et remise d’un rapport détaillé',
     79.00);

-- ==================================================
-- 9) Devis
-- ==================================================
INSERT INTO devis (id_devis, id_demande, date_devis, montant_total) VALUES
                                                                        (1,1,'2025-06-21 14:00:00', 59.90 + 79.00),
                                                                        (2,2,'2025-06-20 15:00:00', 129.90);

-- ==================================================
-- 10) Rendez-vous (statut propre RDV)
-- ==================================================
INSERT INTO rendez_vous (id_rdv, id_demande, id_admin, id_creneau, code_statut) VALUES
                                                                                    (1,3,1,1,'Confirme'),
                                                                                    (2,4,1,3,'Reporte'),
                                                                                    (3,6,1,6,'Annule');

-- ==================================================
-- 11) 🔥 Nouveau cas de test ICS pour Alice (client1)
--     RDV futur (>= aujourd’hui), statut Confirmé, créneau réservé.
-- ==================================================

-- Nouvelle demande de RDV pour Alice
INSERT INTO demande (id_demande, id_client, date_demande, code_type, code_statut)
VALUES (7, 1, '2025-06-25 09:00:00', 'RendezVous', 'En_attente');

-- Services associés (ex: révision complète)
INSERT INTO demande_service (
    id_demande, id_service, quantite,
    libelle_service, description_service, prix_unitaire_service
)
VALUES (7, 2, 1,
        'Révision',
        'Révision générale incluant le contrôle et le remplacement des courroies, filtres (air, habitacle, carburant) et bougies, ainsi que la vérification des niveaux de liquide',
        129.90);

-- Créneau réservé pour ce RDV (futur)
INSERT INTO creneau (id_creneau, date_debut, date_fin, code_statut)
VALUES (7, '2025-10-02 09:00:00', '2025-10-02 10:00:00', 'Reserve');

-- Disponibilité de l’admin sur ce créneau
INSERT INTO disponibilite (id_admin, id_creneau)
VALUES (1, 7);

-- RDV Confirmé pour Alice (future date => visible par findUpcomingByClientId)
INSERT INTO rendez_vous (id_rdv, id_demande, id_admin, id_creneau, code_statut)
VALUES (4, 7, 1, 7, 'Confirme');
