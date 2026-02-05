-- ==================================================
-- 0) Préparation (safe)
-- ==================================================
ALTER TABLE administrateur
    ADD COLUMN IF NOT EXISTS niveau_acces VARCHAR(30) DEFAULT 'PRINCIPAL';

UPDATE administrateur
SET username = concat(prenom, '.', nom, substring(nom from 1 for 1))
WHERE (username IS NULL OR username = '')
  AND prenom IS NOT NULL
  AND nom IS NOT NULL;

UPDATE administrateur
SET niveau_acces = 'ADMIN'
WHERE niveau_acces IS NULL;

-- ==================================================
-- 1) Lookups
-- ==================================================
INSERT INTO type_demande (code_type, libelle) VALUES
                                                  ('Devis', 'Devis'),
                                                  ('Service', 'Service'),
                                                  ('RendezVous', 'Rendez-vous')
ON CONFLICT DO NOTHING;

INSERT INTO service_icon (url, label) VALUES
                                          ('/icons/pictos-metiers/picto-metier-pneu.webp', 'Pneumatiques'),
                                          ('/icons/pictos-metiers/picto-metier-hybride.webp', 'Véhicules hybrides'),
                                          ('/icons/pictos-metiers/picto-metier-geometrie.webp', 'Géométrie'),
                                          ('/icons/pictos-metiers/picto-metier-freinage.webp', 'Freinage'),
                                          ('/icons/pictos-metiers/picto-metier-embrayage.webp', 'Embrayage'),
                                          ('/icons/pictos-metiers/picto-metier-echappement.webp', 'Échappement'),
                                          ('/icons/pictos-metiers/picto-metier-distribution.webp', 'Distribution'),
                                          ('/icons/pictos-metiers/picto-metier-climatisation.webp', 'Climatisation'),
                                          ('/icons/pictos-metiers/picto-metier-amortisseur.webp', 'Amortisseurs'),
                                          ('/icons/pictos-metiers/picto-metier-pre_controle.webp', 'Pré-contrôle technique'),
                                          ('/icons/pictos-metiers/picto-metier-revision_constructeur.webp', 'Révision constructeur'),
                                          ('/icons/pictos-metiers/picto-metier-vidange.webp', 'Vidange'),
                                          ('/icons/pictos-metiers/picto-metier-parebrise.webp', 'Pare-brise')
ON CONFLICT (url) DO NOTHING;

INSERT INTO statut_demande (code_statut, libelle) VALUES
                                                      ('Brouillon', 'Brouillon'),
                                                      ('En_attente', 'En attente'),
                                                      ('Traitee',    'Traitée'),
                                                      ('Annulee',    'Annulée')
ON CONFLICT DO NOTHING;

INSERT INTO statut_creneau (code_statut, libelle) VALUES
                                                      ('Libre',        'Libre'),
                                                      ('Reserve',      'Réservé'),
                                                      ('Indisponible', 'Indisponible')
ON CONFLICT DO NOTHING;

INSERT INTO statut_rendez_vous (code_statut, libelle) VALUES
                                                          ('Confirme', 'Confirmé'),
                                                          ('Reporte',  'Reporté'),
                                                          ('Annule',   'Annulé')
ON CONFLICT DO NOTHING;

-- ==================================================
-- 2) Services
-- ==================================================
INSERT INTO service (
    id_service, libelle, description, id_icon, prix_unitaire,
    quantite_mode, prix_mode, taille_lot,
    quantite_max, archived
) VALUES
      (1, 'Pneumatiques',
       'Montage, équilibrage et réparation de pneumatiques été, hiver ou 4 saisons pour toutes marques de véhicules.',
       1, 89.00, 'LOT', 'LOT', 2, 4, FALSE),

      (2, 'Véhicules hybrides',
       'Interventions sécurisées sur les chaînes de traction et batteries haute tension grâce à nos techniciens habilités.',
       2, 149.00, 'UNIQUE', 'UNITAIRE', NULL, 1, FALSE),

      (3, 'Géométrie',
       'Réglage précis du parallélisme et du carrossage pour préserver vos pneus et garantir une tenue de route optimale.',
       3, 99.00, 'UNIQUE', 'UNITAIRE', NULL, 1, FALSE),

      (4, 'Freinage',
       'Contrôle et remplacement des plaquettes, disques et liquides afin d’assurer un freinage réactif et sécurisant.',
       4, 199.00, 'UNIQUE', 'UNITAIRE', NULL, 1, FALSE),

      (5, 'Embrayage',
       'Diagnostic et remplacement des embrayages, volants moteurs et butées pour une transmission souple et fiable.',
       5, 349.00, 'UNIQUE', 'UNITAIRE', NULL, 1, FALSE),

      (6, 'Échappement',
       'Inspection, réparation et remplacement des lignes d’échappement et filtres à particules pour un moteur sain.',
       6, 129.00, 'UNIQUE', 'UNITAIRE', NULL, 1, FALSE),

      (7, 'Distribution',
       'Remplacement de courroies ou de chaînes de distribution selon les préconisations constructeur.',
       7, 699.00, 'UNIQUE', 'UNITAIRE', NULL, 1, FALSE),

      (8, 'Climatisation',
       'Entretien complet du circuit : recharge, nettoyage, contrôle d’étanchéité et désinfection de l’habitacle.',
       8, 79.00, 'UNIQUE', 'UNITAIRE', NULL, 1, FALSE),

      (9, 'Amortisseurs',
       'Remplacement des amortisseurs, ressorts et biellettes pour une conduite confortable et maîtrisée.',
       9, 249.00, 'UNIQUE', 'UNITAIRE', NULL, 1, FALSE),

      (10, 'Pré-contrôle technique',
       'Préparation complète au contrôle technique avec diagnostic des points de sécurité et corrections nécessaires.',
       10, 59.00, 'UNIQUE', 'UNITAIRE', NULL, 1, FALSE),

      (11, 'Révision constructeur',
       'Révisions certifiées respectant le carnet d’entretien constructeur et l’utilisation de pièces d’origine ou équivalentes.',
       11, 129.90, 'UNIQUE', 'UNITAIRE', NULL, 1, FALSE),

      (12, 'Vidange',
       'Vidanges moteur avec huiles adaptées, remplacement des filtres et remise à zéro des indicateurs d’entretien.',
       12, 59.90, 'UNIQUE', 'UNITAIRE', NULL, 1, FALSE)
ON CONFLICT DO NOTHING;

-- ==================================================
-- 3) Clients (mots de passe déjà hashés)
-- ==================================================
INSERT INTO client (
    id_client, nom, prenom, email, telephone,
    adresse_ligne1, adresse_ligne2, adresse_code_postal, adresse_ville,
    mot_de_passe,
    anonymized,
    email_verified, email_verified_at
) VALUES
      (1,'Durand','Alice','test@client1.fr','0601020304',
       '12 rue Victor Hugo', NULL, '75003', 'Paris',
       '$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW',
       FALSE, TRUE, '2025-06-01 10:00:00'),

      (2,'Martin','Bob','test@client2.fr','0605060708',
       '45 av. Jean Jaurès', NULL, '69007', 'Lyon',
       '$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW',
       FALSE, TRUE, '2025-06-01 10:00:00'),

      (3,'Bernard','Claire','test@client3.fr','0611121314',
       '78 bd Haussmann', NULL, '75009', 'Paris',
       '$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW',
       FALSE, TRUE, '2025-06-01 10:00:00'),

      (4,'Lefevre','David','test@client4.fr','0622232425',
       '3 place Bellecour', NULL, '69002', 'Lyon',
       '$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW',
       FALSE, TRUE, '2025-06-01 10:00:00'),

      (5,'Dupont','Eva','test@client5.fr','0633343536',
       '6 quai de la Loire', NULL, '44000', 'Nantes',
       '$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW',
       FALSE, FALSE, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO client_vehicle (
    id_vehicle, id_client, immatriculation,
    vehicule_marque, vehicule_modele, vehicule_energie
) VALUES
      (1, 1, 'AA-123-AA', 'Peugeot', '208', 'ESSENCE'),
      (2, 2, 'BB-234-BB', 'Renault', 'Clio', 'DIESEL'),
      (3, 3, 'CC-345-CC', 'Citroen', 'C3', 'ESSENCE'),
      (4, 4, 'DD-456-DD', 'Volkswagen', 'Golf', 'DIESEL'),
      (5, 5, 'EE-567-EE', 'Tesla', 'Model 3', 'ELECTRIQUE')
ON CONFLICT DO NOTHING;

-- ==================================================
-- 4) Administrateurs (ressources)
-- ==================================================
INSERT INTO administrateur (id_admin, username, email, mot_de_passe, nom, prenom, niveau_acces) VALUES
    (1,'Michael.B','test@admin.fr',
     '$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW',
     'Bongeot','Michael', 'PRINCIPAL'),

    (2,'Test.Gestionnaire','test@gestionnaire.fr',
     '$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW',
     'Gestionnaire','Gestionnaire', 'GESTIONNAIRE'),

    (3,'Flo.S','test@sous-admin.fr',
     '$2a$10$KIjgzG.nEJCuPd2Dx0.peuC4q1aQfHPHvv5ODXrzqMLe0QR7LhtGW',
     'Flo','Super', 'ADMIN')
ON CONFLICT DO NOTHING;

-- ==================================================
-- 4.1) Horaires d'ouverture du garage
-- ==================================================
INSERT INTO garage_opening_hour (
    id_opening_hour, scope, status, opening_type, day_of_week,
    exceptional_type, exceptional_date, exceptional_start_date, exceptional_end_date, label, description,
    start_time, end_time, start_time_2, end_time_2
) VALUES
      (1, 'ANNUAL', 'OPEN', 'SPLIT', 'MONDAY', NULL, NULL, NULL, NULL, NULL, NULL,
       '08:30:00', '12:00:00', '13:30:00', '18:00:00'),
      (2, 'ANNUAL', 'OPEN', 'SPLIT', 'TUESDAY', NULL, NULL, NULL, NULL, NULL, NULL,
       '08:30:00', '12:00:00', '13:30:00', '18:00:00'),
      (3, 'ANNUAL', 'OPEN', 'SPLIT', 'WEDNESDAY', NULL, NULL, NULL, NULL, NULL, NULL,
       '08:30:00', '12:00:00', '13:30:00', '18:00:00'),
      (4, 'ANNUAL', 'OPEN', 'SPLIT', 'THURSDAY', NULL, NULL, NULL, NULL, NULL, NULL,
       '08:30:00', '12:00:00', '13:30:00', '18:00:00'),
      (5, 'ANNUAL', 'OPEN', 'SPLIT', 'FRIDAY', NULL, NULL, NULL, NULL, NULL, NULL,
       '08:30:00', '12:00:00', '13:30:00', '18:00:00'),
      (6, 'ANNUAL', 'CLOSED', NULL, 'SATURDAY', NULL, NULL, NULL, NULL, NULL, NULL,
       NULL, NULL, NULL, NULL),
      (7, 'ANNUAL', 'CLOSED', NULL, 'SUNDAY', NULL, NULL, NULL, NULL, NULL, NULL,
       NULL, NULL, NULL, NULL)
ON CONFLICT DO NOTHING;

-- ==================================================
-- 5) Créneaux (1 créneau par ressource / admin)
--     Règles:
--     - Confirme => creneau Reserve
--     - Reporte  => RDV pointe sur nouveau créneau Reserve, ancien créneau Libre
--     - Annule   => creneau Libre (ne bloque pas)
-- ==================================================

-- 2026-07-01 09:00-10:00
INSERT INTO creneau (id_creneau, date_debut, date_fin, code_statut) VALUES
    (101,'2026-07-01 09:00:00','2026-07-01 10:00:00','Reserve'), -- admin 1 (RDV confirmé)
    (102,'2026-07-01 09:00:00','2026-07-01 10:00:00','Libre'),   -- admin 2
    (103,'2026-07-01 09:00:00','2026-07-01 10:00:00','Libre')    -- admin 3
ON CONFLICT DO NOTHING;

-- 2026-07-01 10:00-11:00
INSERT INTO creneau (id_creneau, date_debut, date_fin, code_statut) VALUES
    (104,'2026-07-01 10:00:00','2026-07-01 11:00:00','Reserve'), -- admin 1 (RDV confirmé)
    (105,'2026-07-01 10:00:00','2026-07-01 11:00:00','Libre'),
    (106,'2026-07-01 10:00:00','2026-07-01 11:00:00','Libre')
ON CONFLICT DO NOTHING;

-- 2026-07-01 11:00-12:00 (créneau initialement prévu pour le RDV reporté => doit être LIBRE après report)
INSERT INTO creneau (id_creneau, date_debut, date_fin, code_statut) VALUES
    (107,'2026-07-01 11:00:00','2026-07-01 12:00:00','Libre'), -- admin 1 (libéré après report)
    (108,'2026-07-01 11:00:00','2026-07-01 12:00:00','Libre'),
    (109,'2026-07-01 11:00:00','2026-07-01 12:00:00','Libre')
ON CONFLICT DO NOTHING;

-- 2026-07-03 09:00-10:00 (nouveau créneau du RDV reporté)
INSERT INTO creneau (id_creneau, date_debut, date_fin, code_statut) VALUES
    (130,'2026-07-03 09:00:00','2026-07-03 10:00:00','Reserve'), -- admin 1 (RDV reporté)
    (131,'2026-07-03 09:00:00','2026-07-03 10:00:00','Libre'),
    (132,'2026-07-03 09:00:00','2026-07-03 10:00:00','Libre')
ON CONFLICT DO NOTHING;

-- 2026-07-01 14:00-15:00 (admin 1 indispo)
INSERT INTO creneau (id_creneau, date_debut, date_fin, code_statut) VALUES
    (110,'2026-07-01 14:00:00','2026-07-01 15:00:00','Indisponible'),
    (111,'2026-07-01 14:00:00','2026-07-01 15:00:00','Libre'),
    (112,'2026-07-01 14:00:00','2026-07-01 15:00:00','Libre')
ON CONFLICT DO NOTHING;

-- 2026-07-02 09:00-10:00 (admin 1 indispo)
INSERT INTO creneau (id_creneau, date_debut, date_fin, code_statut) VALUES
    (113,'2026-07-02 09:00:00','2026-07-02 10:00:00','Indisponible'),
    (114,'2026-07-02 09:00:00','2026-07-02 10:00:00','Libre'),
    (115,'2026-07-02 09:00:00','2026-07-02 10:00:00','Libre')
ON CONFLICT DO NOTHING;

-- 2026-07-02 10:00-11:00 (RDV annulé => créneau Libre)
INSERT INTO creneau (id_creneau, date_debut, date_fin, code_statut) VALUES
    (116,'2026-07-02 10:00:00','2026-07-02 11:00:00','Libre'), -- admin 1 (RDV annulé)
    (117,'2026-07-02 10:00:00','2026-07-02 11:00:00','Libre'),
    (118,'2026-07-02 10:00:00','2026-07-02 11:00:00','Libre')
ON CONFLICT DO NOTHING;

-- ==================================================
-- 6) Disponibilités (planning par ressource)
-- ==================================================
INSERT INTO disponibilite (id_admin, id_creneau) VALUES
    -- admin 1
    (1,101),(1,104),(1,107),(1,110),(1,113),(1,116),(1,130),

    -- admin 2
    (2,102),(2,105),(2,108),(2,111),(2,114),(2,117),(2,131),

    -- admin 3
    (3,103),(3,106),(3,109),(3,112),(3,115),(3,118),(3,132)
ON CONFLICT DO NOTHING;

-- ==================================================
-- 7) Demandes
-- ==================================================
INSERT INTO demande (id_demande, id_client, date_demande, code_type, code_statut) VALUES
    (1, 1, '2026-06-20 08:15:00', 'Devis',      'Traitee'),
    (2, 2, '2026-06-19 09:30:00', 'Devis',      'Traitee'),
    (3, 3, '2026-06-18 10:45:00', 'RendezVous', 'Traitee'),
    (4, 4, '2026-06-17 11:00:00', 'RendezVous', 'En_attente'),
    (5, 5, '2026-06-16 12:00:00', 'Devis',      'Annulee'),
    (6, 1, '2026-06-15 13:00:00', 'RendezVous', 'Annulee')
ON CONFLICT DO NOTHING;

-- ==================================================
-- 8) Demande_Service
-- ==================================================
INSERT INTO demande_service (
    id_demande, id_service, quantite,
    libelle_service, description_service, prix_unitaire_service
) VALUES
      -- Demande 1 : Alice (Vidange + Pré-contrôle)
      (1, 12, 1, 'Vidange',
       'Vidanges moteur avec huiles adaptées, remplacement des filtres et remise à zéro des indicateurs d’entretien.',
       59.90),
      (1, 10, 1, 'Pré-contrôle technique',
       'Préparation complète au contrôle technique avec diagnostic des points de sécurité et corrections nécessaires.',
       59.00),

      -- Demande 2 : Bob (Révision)
      (2, 11, 1, 'Révision constructeur',
       'Révisions certifiées respectant le carnet d’entretien constructeur et l’utilisation de pièces d’origine ou équivalentes.',
       129.90),

      -- Demande 3 : Claire (Pneus)
      (3, 1, 1, 'Pneumatiques',
       'Montage, équilibrage et réparation de pneumatiques été, hiver ou 4 saisons pour toutes marques de véhicules.',
       89.00),

      -- Demande 5 : Eva (Géométrie) - demande annulée mais on laisse l'historique
      (5, 3, 1, 'Géométrie',
       'Réglage précis du parallélisme et du carrossage pour préserver vos pneus et garantir une tenue de route optimale.',
       99.00)
ON CONFLICT DO NOTHING;

-- ==================================================
-- 9) Devis
-- ==================================================
INSERT INTO devis (id_devis, id_demande, date_devis, montant_total, id_rdv) VALUES
    (1, 1, '2026-06-21 14:00:00', 138.90, 5),
    (2, 2, '2026-06-20 15:00:00', 129.90, NULL)
ON CONFLICT DO NOTHING;

-- ==================================================
-- 10) Rendez-vous (cohérents avec les créneaux)
-- ==================================================
INSERT INTO rendez_vous (
    id_rdv, id_demande, id_admin, id_creneau, code_statut, commentaire, id_devis, id_demande_service, id_service
) VALUES
      -- RDV confirmé : Claire
      (1, 3, 1, 101, 'Confirme', 'Contrôle général avant départ en vacances.', NULL, NULL, NULL),

      -- RDV reporté : David -> NOUVELLE DATE 2026-07-03 09:00 (id_creneau=130)
      (2, 4, 1, 130, 'Reporte',  'Demande de vérification du freinage. (Reporté au 03/07 09:00)', NULL, NULL, NULL),

      -- RDV annulé : Alice -> créneau doit rester Libre (id_creneau=116)
      (3, 6, 1, 116, 'Annule',   NULL, NULL, NULL, NULL),

      -- RDV confirmé suite devis : Alice
      (5, 1, 1, 104, 'Confirme', 'Rendez-vous suite au devis validé.', 1, NULL, NULL)
ON CONFLICT DO NOTHING;

-- ==================================================
-- 11) Documents et timeline des demandes
-- ==================================================
INSERT INTO demande_document (
    id_document, id_demande, nom_fichier, url_private, type_contenu,
    taille_octets, visible_client, cree_par, cree_par_role, cree_le
) VALUES
    (1, 1, 'devis_jlh_autopam_test.pdf',
     'documents/2b6409c4-8973-4446-ad79-d7a716a61006_devis_jlh_autopam_test.pdf',
     'application/pdf', 20480, TRUE,
     'Michael', 'ADMIN', '2026-06-20 09:00:00')
ON CONFLICT DO NOTHING;

INSERT INTO demande_timeline (
    id_timeline, id_demande, type_evenement, cree_le, cree_par, cree_par_role, visible_client,
    statut_code, statut_libelle, commentaire, montant_valide,
    document_id, document_nom, document_url,
    rendezvous_id, rendezvous_statut_code, rendezvous_statut_libelle, rendezvous_date_debut, rendezvous_date_fin
) VALUES
      (1, 1, 'MONTANT', '2026-06-20 08:30:00', 'test@admin.fr', 'ADMIN', TRUE,
       'En_attente', 'En attente', 'Création du devis', 138.90,
       NULL, NULL, NULL,
       NULL, NULL, NULL, NULL, NULL),

      (2, 1, 'DOCUMENT', '2026-06-20 09:00:00', 'test@admin.fr', 'ADMIN', TRUE,
       NULL, NULL, 'Ajout du contrôle technique', NULL,
       1, 'devis_jlh_autopam_test.pdf', 'uploads/documents/devis_jlh_autopam_test',
       NULL, NULL, NULL, NULL, NULL),

      (3, 1, 'RENDEZVOUS', '2026-07-01 10:00:00', 'test@admin.fr', 'ADMIN', TRUE,
       NULL, NULL, 'Rendez-vous confirmé suite au devis', NULL,
       NULL, NULL, NULL,
       5, 'Confirme', 'Confirmé', '2026-07-01 10:00:00', '2026-07-01 11:00:00')
ON CONFLICT DO NOTHING;

-- ==================================================
-- 12) Cas de test ICS pour Alice (client1)
-- ==================================================
INSERT INTO demande (id_demande, id_client, date_demande, code_type, code_statut)
VALUES (7, 1, '2026-06-25 09:00:00', 'RendezVous', 'En_attente')
ON CONFLICT DO NOTHING;

INSERT INTO demande_service (
    id_demande, id_service, quantite,
    libelle_service, description_service, prix_unitaire_service
)
VALUES (7, 11, 1,
        'Révision constructeur',
        'Révisions certifiées respectant le carnet d’entretien constructeur et l’utilisation de pièces d’origine ou équivalentes.',
        129.90)
ON CONFLICT DO NOTHING;

-- 2026-10-02 09:00-10:00 : 3 créneaux, un seul réservé (admin 1)
INSERT INTO creneau (id_creneau, date_debut, date_fin, code_statut) VALUES
    (201, '2026-10-02 09:00:00', '2026-10-02 10:00:00', 'Reserve'),
    (202, '2026-10-02 09:00:00', '2026-10-02 10:00:00', 'Libre'),
    (203, '2026-10-02 09:00:00', '2026-10-02 10:00:00', 'Libre')
ON CONFLICT DO NOTHING;

INSERT INTO disponibilite (id_admin, id_creneau) VALUES
                                                     (1,201),(2,202),(3,203)
ON CONFLICT DO NOTHING;

INSERT INTO rendez_vous (id_rdv, id_demande, id_admin, id_creneau, code_statut, commentaire)
VALUES (4, 7, 1, 201, 'Confirme', 'Révision complète avant contrôle technique.')
ON CONFLICT DO NOTHING;

-- ==================================================
-- 13) Historique (simplifié et cohérent)
--     Nettoyage: on évite les créneaux partagés ambiguës -> 1 créneau par admin pour les RDV confirmés
-- ==================================================

-- Créneaux historiques (uniquement admin 1 pour stats, tu peux dupliquer pour 2/3 si tu veux)
INSERT INTO creneau (id_creneau, date_debut, date_fin, code_statut) VALUES
    (301, '2025-04-15 09:00:00', '2025-04-15 10:30:00', 'Indisponible'),
    (302, '2024-06-12 08:30:00', '2024-06-12 09:30:00', 'Reserve'),
    (303, '2023-05-05 14:00:00', '2023-05-05 15:00:00', 'Reserve'),
    (304, '2022-11-12 10:00:00', '2022-11-12 11:00:00', 'Indisponible'),
    (305, '2024-03-10 15:00:00', '2024-03-10 16:00:00', 'Indisponible')
ON CONFLICT DO NOTHING;

INSERT INTO disponibilite (id_admin, id_creneau) VALUES
    (1,301),(1,302),(1,303),(1,304),(1,305)
ON CONFLICT DO NOTHING;

INSERT INTO demande (id_demande, id_client, date_demande, code_type, code_statut) VALUES
    (8, 2, '2025-04-10 09:00:00', 'Service',    'Traitee'),
    (9, 3, '2024-03-05 11:00:00', 'Devis',      'Traitee'),
    (10,4, '2023-05-01 08:30:00', 'RendezVous', 'Traitee'),
    (11,5, '2022-11-01 14:00:00', 'Service',    'Traitee'),
    (12,1, '2025-09-01 10:15:00', 'Devis',      'Traitee'),
    (13,2, '2024-06-10 16:00:00', 'RendezVous', 'Traitee')
ON CONFLICT DO NOTHING;

INSERT INTO demande_service (
    id_demande, id_service, quantite,
    libelle_service, description_service, prix_unitaire_service
) VALUES
      (8, 2, 1, 'Véhicules hybrides',
       'Interventions sécurisées sur les chaînes de traction et batteries haute tension grâce à nos techniciens habilités.',
       149.00),
      (9, 4, 2, 'Freinage',
       'Contrôle et remplacement des plaquettes, disques et liquides afin d’assurer un freinage réactif et sécurisant.',
       199.00),
      (10, 1, 4, 'Pneumatiques',
       'Montage, équilibrage et réparation de pneumatiques été, hiver ou 4 saisons pour toutes marques de véhicules.',
       89.00),
      (11, 12, 1, 'Vidange',
       'Vidanges moteur avec huiles adaptées, remplacement des filtres et remise à zéro des indicateurs d’entretien.',
       59.90),
      (12, 7, 1, 'Distribution',
       'Remplacement de courroies ou de chaînes de distribution selon les préconisations constructeur.',
       699.00),
      (13, 3, 1, 'Géométrie',
       'Réglage précis du parallélisme et du carrossage pour préserver vos pneus et garantir une tenue de route optimale.',
       99.00)
ON CONFLICT DO NOTHING;

INSERT INTO devis (id_devis, id_demande, date_devis, montant_total) VALUES
    (3, 9, '2024-03-06 09:00:00', 398.00),
    (4,12, '2025-09-02 09:30:00', 699.00)
ON CONFLICT DO NOTHING;

INSERT INTO rendez_vous (id_rdv, id_demande, id_admin, id_creneau, code_statut, commentaire) VALUES
    (8, 10, 1, 303, 'Confirme', 'Remplacement pneus saisonniers.'),
    (9, 13, 1, 302, 'Confirme', 'Contrôle géométrie après intervention.')
ON CONFLICT DO NOTHING;

-- ==================================================
-- 14) Services réalisés + avis clients
-- ==================================================
INSERT INTO demande (id_demande, id_client, date_demande, code_type, code_statut) VALUES
    (14, 1, '2025-02-20 09:00:00', 'Service', 'Traitee'),
    (15, 2, '2025-03-14 14:30:00', 'Service', 'Traitee'),
    (16, 3, '2024-11-08 08:45:00', 'Service', 'Traitee')
ON CONFLICT DO NOTHING;

INSERT INTO demande_service (
    id_demande, id_service, quantite,
    libelle_service, description_service, prix_unitaire_service
) VALUES
      (14, 12, 1, 'Vidange',
       'Vidanges moteur avec huiles adaptées, remplacement des filtres et remise à zéro des indicateurs d’entretien.',
       59.90),
      (15, 4, 1, 'Freinage',
       'Contrôle et remplacement des plaquettes, disques et liquides afin d’assurer un freinage réactif et sécurisant.',
       199.00),
      (16, 8, 1, 'Climatisation',
       'Entretien complet du circuit : recharge, nettoyage, contrôle d’étanchéité et désinfection de l’habitacle.',
       79.00)
ON CONFLICT DO NOTHING;

INSERT INTO avis_service (
    id_avis, id_demande, id_service, id_client, note, commentaire, cree_le, statut
) VALUES
      (1, 14, 12, 1, 5, 'Service rapide et explications claires.', '2025-02-21 10:00:00', 'APPROVED'),
      (2, 15, 4, 2, 4, 'Freinage remis à neuf, très satisfait.',      '2025-03-15 09:30:00', 'APPROVED'),
      (3, 16, 8, 3, 5, 'Climatisation impeccable après intervention.', '2024-11-09 11:15:00', 'APPROVED')
ON CONFLICT DO NOTHING;

-- Resynchronise la sequence après les insertions avec identifiants forcés
SELECT setval(
    pg_get_serial_sequence('demande', 'id_demande'),
    COALESCE((SELECT MAX(id_demande) FROM demande), 1),
    true
);
