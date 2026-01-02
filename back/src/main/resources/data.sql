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

-- Ajout colonnes vehicule_marque / vehicule_modele
SET @exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'client' AND COLUMN_NAME = 'vehicule_marque'
);
SET @ddl := IF(@exists = 0,
               'ALTER TABLE client ADD COLUMN vehicule_marque VARCHAR(100) NULL',
               'SELECT 1'
            );
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'client' AND COLUMN_NAME = 'vehicule_modele'
);
SET @ddl := IF(@exists = 0,
               'ALTER TABLE client ADD COLUMN vehicule_modele VARCHAR(100) NULL',
               'SELECT 1'
            );
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Ajout colonne username (nullable au départ pour éviter l'échec si la table contient déjà des données)
SET @exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'administrateur' AND COLUMN_NAME = 'username'
);
SET @ddl := IF(@exists = 0,
               'ALTER TABLE administrateur ADD COLUMN username VARCHAR(50) UNIQUE',
               'SELECT 1'
            );
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Ajout colonne icon sur les services (optionnelle)
SET @exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'service' AND COLUMN_NAME = 'icon'
);
SET @ddl := IF(@exists = 0,
               'ALTER TABLE service ADD COLUMN icon VARCHAR(255) NULL',
               'SELECT 1'
            );
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Ajout colonne commentaire sur les rendez-vous (optionnelle)
SET @exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'rendez_vous' AND COLUMN_NAME = 'commentaire'
);
SET @ddl := IF(@exists = 0,
               'ALTER TABLE rendez_vous ADD COLUMN commentaire TEXT NULL',
               'SELECT 1'
            );
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Valorise username à partir de l'e-mail pour les administrateurs existants
UPDATE administrateur
SET username = concat(prenom, '.', nom, substr(nom, 1,1))
WHERE username IS NULL OR username = '';

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
INSERT INTO service (id_service, libelle, description, icon, prix_unitaire, quantite_max, archived) VALUES
                                                                          (1, 'Pneumatiques',
                                                                           'Montage, équilibrage et réparation de pneumatiques été, hiver ou 4 saisons pour toutes marques de véhicules.',
                                                                           'assets/icons/picto-metier-pneu.png',
                                                                           89.00, 4, 0),
                                                                          (2, 'Véhicules hybrides',
                                                                           'Interventions sécurisées sur les chaînes de traction et batteries haute tension grâce à nos techniciens habilités.',
                                                                           'assets/icons/picto-metier-hybride.png',
                                                                           149.00, 1, 0),
                                                                          (3, 'Géométrie',
                                                                           'Réglage précis du parallélisme et du carrossage pour préserver vos pneus et garantir une tenue de route optimale.',
                                                                           'assets/icons/picto-metier-geometrie.png',
                                                                           99.00, 1, 0),
                                                                          (4, 'Freinage',
                                                                           'Contrôle et remplacement des plaquettes, disques et liquides afin d’assurer un freinage réactif et sécurisant.',
                                                                           'assets/icons/picto-metier-freinage.png',
                                                                           199.00, 2, 0),
                                                                          (5, 'Embrayage',
                                                                           'Diagnostic et remplacement des embrayages, volants moteurs et butées pour une transmission souple et fiable.',
                                                                           'assets/icons/picto-metier-embrayage.png',
                                                                           349.00, 1, 0),
                                                                          (6, 'Échappement',
                                                                           'Inspection, réparation et remplacement des lignes d’échappement et filtres à particules pour un moteur sain.',
                                                                           'assets/icons/picto-metier-echappement.png',
                                                                           129.00, 1, 0),
                                                                          (7, 'Distribution',
                                                                           'Remplacement de courroies ou de chaînes de distribution selon les préconisations constructeur.',
                                                                           'assets/icons/picto-metier-distribution.png',
                                                                           699.00, 1, 0),
                                                                          (8, 'Climatisation',
                                                                           'Entretien complet du circuit : recharge, nettoyage, contrôle d’étanchéité et désinfection de l’habitacle.',
                                                                           'assets/icons/picto-metier-climatisation.png',
                                                                           79.00, 1, 0),
                                                                          (9, 'Amortisseurs',
                                                                           'Remplacement des amortisseurs, ressorts et biellettes pour une conduite confortable et maîtrisée.',
                                                                           'assets/icons/picto-metier-amortisseur.png',
                                                                           249.00, 2, 0),
                                                                          (10, 'Pré-contrôle technique',
                                                                           'Préparation complète au contrôle technique avec diagnostic des points de sécurité et corrections nécessaires.',
                                                                           'assets/icons/picto-metier-pre_controle.png',
                                                                           59.00, 1, 0),
                                                                          (11, 'Révision constructeur',
                                                                           'Révisions certifiées respectant le carnet d’entretien constructeur et l’utilisation de pièces d’origine ou équivalentes.',
                                                                           'assets/icons/picto-metier-revision_constructeur.png',
                                                                           129.90, 1, 0),
                                                                          (12, 'Vidange',
                                                                           'Vidanges moteur avec huiles adaptées, remplacement des filtres et remise à zéro des indicateurs d’entretien.',
                                                                           'assets/icons/picto-metier-vidange.png',
                                                                           59.90, 1, 0);

-- ==================================================
-- 3) Clients (mots de passe déjà hashés)
--    On renseigne explicitement email_verified / email_verified_at
-- ==================================================
INSERT INTO client (
    id_client, nom, prenom, email, telephone,
    adresse_ligne1, adresse_ligne2, adresse_code_postal, adresse_ville,
    immatriculation, vehicule_marque, vehicule_modele, mot_de_passe,
    email_verified, email_verified_at
) VALUES
      (1,'Durand','Alice','test@client1.fr','0601020304',
       '12 rue Victor Hugo', NULL, '75003', 'Paris',
       'AA-123-AA','Peugeot','208',
       '$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW',
       1, '2025-06-01 10:00:00'),

      (2,'Martin','Bob','test@client2.fr','0605060708',
       '45 av. Jean Jaurès', NULL, '69007', 'Lyon',
       'BB-234-BB','Renault','Clio',
       '$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW',
       1, '2025-06-01 10:00:00'),

      (3,'Bernard','Claire','test@client3.fr','0611121314',
       '78 bd Haussmann', NULL, '75009', 'Paris',
       'CC-345-CC','Citroen','C3',
       '$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW',
       1, '2025-06-01 10:00:00'),

      (4,'Lefevre','David','test@client4.fr','0622232425',
       '3 place Bellecour', NULL, '69002', 'Lyon',
       'DD-456-DD','Volkswagen','Golf',
       '$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW',
       1, '2025-06-01 10:00:00'),

      (5,'Dupont','Eva','test@client5.fr','0633343536',
       '6 quai de la Loire', NULL, '44000', 'Nantes',
       'EE-567-EE','Tesla','Model 3',
       '$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW',
       0, NULL);


-- ==================================================
-- 4) Admins
-- ==================================================
INSERT INTO administrateur (id_admin, username, email, mot_de_passe, nom, prenom) VALUES
    (1,'Michael.B','test@admin.fr','$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW','Bongeot','Michael');

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
    (1, 12, 1,
     'Vidange',
     'Vidanges moteur avec huiles adaptées, remplacement des filtres et remise à zéro des indicateurs d’entretien.',
     59.90),
    (1, 10, 1,
     'Pré-contrôle technique',
     'Préparation complète au contrôle technique avec diagnostic des points de sécurité et corrections nécessaires.',
     59.00);

-- Demande 2 : Bob veut Révision
INSERT INTO demande_service (
    id_demande, id_service, quantite,
    libelle_service, description_service, prix_unitaire_service
) VALUES
    (2, 11, 1,
     'Révision constructeur',
     'Révisions certifiées respectant le carnet d’entretien constructeur et l’utilisation de pièces d’origine ou équivalentes.',
     129.90);

-- Demande 3 : Claire veut Changement pneus x4
INSERT INTO demande_service (
    id_demande, id_service, quantite,
    libelle_service, description_service, prix_unitaire_service
) VALUES
    (3, 1, 1,
     'Pneumatiques',
     'Montage, équilibrage et réparation de pneumatiques été, hiver ou 4 saisons pour toutes marques de véhicules.',
     89.00);

-- Demande 5 : Eva voulait Diagnostic
INSERT INTO demande_service (
    id_demande, id_service, quantite,
    libelle_service, description_service, prix_unitaire_service
) VALUES
    (5, 3, 1,
     'Géométrie',
     'Réglage précis du parallélisme et du carrossage pour préserver vos pneus et garantir une tenue de route optimale.',
     99.00);

-- ==================================================
-- 9) Devis
-- ==================================================
INSERT INTO devis (id_devis, id_demande, date_devis, montant_total) VALUES
                                                                        (1,1,'2025-06-21 14:00:00', 59.90 + 59.00),
                                                                        (2,2,'2025-06-20 15:00:00', 129.90);

-- ==================================================
-- 10) Rendez-vous (statut propre RDV)
-- ==================================================
INSERT INTO rendez_vous (id_rdv, id_demande, id_admin, id_creneau, code_statut, commentaire) VALUES
                                                                                    (1,3,1,1,'Confirme', 'Contrôle général avant départ en vacances.'),
                                                                                    (2,4,1,3,'Reporte', 'Demande de vérification du freinage.'),
                                                                                    (3,6,1,6,'Annule', NULL);

-- ==================================================
-- 10) Documents et timeline des demandes
-- ==================================================
INSERT INTO demande_document (
    id_document, id_demande, nom_fichier, url_private, type_contenu,
    taille_octets, visible_client, cree_par, cree_par_role, cree_le
) VALUES (
    1, 1, 'devis_jlh_autopam_test.pdf',
    'documents/2b6409c4-8973-4446-ad79-d7a716a61006_devis_jlh_autopam_test.pdf',
    'application/pdf', 20480, 1,
    'Michael', 'ADMIN', '2025-06-20 09:00:00'
);

INSERT INTO demande_timeline (
    id_timeline, id_demande, type_evenement, cree_le, cree_par, cree_par_role, visible_client,
    statut_code, statut_libelle, commentaire, montant_valide,
    document_id, document_nom, document_url,
    rendezvous_id, rendezvous_statut_code, rendezvous_statut_libelle, rendezvous_date_debut, rendezvous_date_fin
) VALUES
    (1, 1, 'MONTANT', '2025-06-20 08:30:00', 'test@admin.fr', 'ADMIN', 1,
     'En_attente', 'En attente', 'Création du devis', 138.90,
     NULL, NULL, NULL,
     NULL, NULL, NULL, NULL, NULL),
    (2, 1, 'DOCUMENT', '2025-06-20 09:00:00', 'test@admin.fr', 'ADMIN', 1,
     NULL, NULL, 'Ajout du contrôle technique', NULL,
     1, 'devis_jlh_autopam_test.pdf', 'uploads/documents/devis_jlh_autopam_test',
     NULL, NULL, NULL, NULL, NULL);

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
VALUES (7, 11, 1,
        'Révision constructeur',
        'Révisions certifiées respectant le carnet d’entretien constructeur et l’utilisation de pièces d’origine ou équivalentes.',
        129.90);

-- Créneau réservé pour ce RDV (futur)
INSERT INTO creneau (id_creneau, date_debut, date_fin, code_statut)
VALUES (7, '2025-10-02 09:00:00', '2025-10-02 10:00:00', 'Reserve');

-- Disponibilité de l’admin sur ce créneau
INSERT INTO disponibilite (id_admin, id_creneau)
VALUES (1, 7);

-- RDV Confirmé pour Alice (future date => visible par findUpcomingByClientId)
INSERT INTO rendez_vous (id_rdv, id_demande, id_admin, id_creneau, code_statut, commentaire)
VALUES (4, 7, 1, 7, 'Confirme', 'Révision complète avant contrôle technique.');
