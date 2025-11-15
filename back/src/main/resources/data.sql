-- --------------------------------------------------
-- Lookup tables : Type_Demande, Statut_Demande, Statut_Creneau, Statut_RendezVous
-- --------------------------------------------------

INSERT INTO type_demande (code_type, libelle) VALUES
    ('Devis',       'Devis'),
    ('RendezVous',  'Rendez-vous');

INSERT INTO statut_demande (code_statut, libelle) VALUES
    ('En_attente',  'En attente'),
    ('Traitee',     'Traitée'),
    ('Annulee',     'Annulée');

INSERT INTO statut_creneau (code_statut, libelle) VALUES
    ('Libre',        'Libre'),
    ('Reserve',      'Réservé'),
    ('Indisponible', 'Indisponible');

INSERT INTO statut_rendez_vous (code_statut, libelle) VALUES
    ('Confirme', 'Confirmé'),
    ('Reporte',  'Reporté'),
    ('Annule',   'Annulé');


-- --------------------------------------------------
-- Services
-- --------------------------------------------------

INSERT INTO service (id_service, libelle, description, prix_unitaire) VALUES
    (1, 'Vidange',         'Vidange complète avec filtre',       59.90),
    (2, 'Révision',        'Révision générale (courroies, filtres)', 129.90),
    (3, 'Freinage',        'Changement plaquettes avant',         199.00),
    (4, 'Pneumatiques',    'Remplacement 4 pneus toutes saisons', 449.00),
    (5, 'Diagnostic',      'Diagnostic électronique complet',      79.00);


-- --------------------------------------------------
-- Clients
-- --------------------------------------------------

INSERT INTO client (id_client, nom, prenom, email, telephone, adresse) VALUES
    (1, 'Durand',    'Alice',  'alice.durand@example.com',  '0601020304', '12 rue Victor Hugo, 75003 Paris'),
    (2, 'Martin',    'Bob',    'bob.martin@example.com',    '0605060708', '45 avenue Jean Jaurès, 69007 Lyon'),
    (3, 'Bernard',   'Claire', 'claire.bernard@example.com','0611121314', '78 boulevard Haussmann, 75009 Paris'),
    (4, 'Lefevre',   'David',  'david.lefevre@example.com', '0622232425', '3 place Bellecour, 69002 Lyon'),
    (5, 'Dupont',    'Eva',    'eva.dupont@example.com',    '0633343536', '6 quai de la Loire, 44000 Nantes');


-- --------------------------------------------------
-- Administrateurs
-- --------------------------------------------------

INSERT INTO administrateur (id_admin, username, mot_de_passe, nom, prenom) VALUES
    (1, 'admin1', 'password1', 'Admin', 'Un'),
    (2, 'admin2', 'password2', 'Admin', 'Deux');


-- --------------------------------------------------
-- Créneaux
-- --------------------------------------------------

INSERT INTO creneau (id_creneau, date_debut, date_fin, code_statut) VALUES
    (1, '2025-07-01 09:00:00', '2025-07-01 10:00:00', 'Libre'),
    (2, '2025-07-01 10:00:00', '2025-07-01 11:00:00', 'Libre'),
    (3, '2025-07-01 11:00:00', '2025-07-01 12:00:00', 'Reserve'),
    (4, '2025-07-01 14:00:00', '2025-07-01 15:00:00', 'Indisponible'),
    (5, '2025-07-02 09:00:00', '2025-07-02 10:00:00', 'Libre'),
    (6, '2025-07-02 10:00:00', '2025-07-02 11:00:00', 'Reserve');


-- --------------------------------------------------
-- Disponibilités (admins ↔ créneaux)
-- --------------------------------------------------

INSERT INTO disponibilite (id_admin, id_creneau) VALUES
    (1, 1),
    (1, 2),
    (1, 3),
    (2, 5),
    (2, 6);


-- --------------------------------------------------
-- Demandes
-- --------------------------------------------------

-- Devis: Alice (En attente), multiple services
INSERT INTO demande (id_demande, id_client, date_soumission, code_type, code_statut) VALUES
    (1, 1, '2025-06-20 08:15:00', 'Devis',     'En_attente');

-- Devis: Bob (Traitée)
INSERT INTO demande (id_demande, id_client, date_soumission, code_type, code_statut) VALUES
    (2, 2, '2025-06-19 09:30:00', 'Devis',     'Traitee');

-- RDV: Claire (Confirmé)
INSERT INTO demande (id_demande, id_client, date_soumission, code_type, code_statut) VALUES
    (3, 3, '2025-06-18 10:45:00', 'RendezVous','En_attente');

-- RDV: David (Reporté)
INSERT INTO demande (id_demande, id_client, date_soumission, code_type, code_statut) VALUES
    (4, 4, '2025-06-17 11:00:00', 'RendezVous','Traitee');

-- Devis: Eva (Annulée)
INSERT INTO demande (id_demande, id_client, date_soumission, code_type, code_statut) VALUES
    (5, 5, '2025-06-16 12:00:00', 'Devis',     'Annulee');

-- RDV: Alice (Annulé)
INSERT INTO demande (id_demande, id_client, date_soumission, code_type, code_statut) VALUES
    (6, 1, '2025-06-15 13:00:00', 'RendezVous','Annulee');


-- --------------------------------------------------
-- Demande_Service (liaisons)
-- --------------------------------------------------

-- Demande 1 : Alice veut Vidange + Diagnostic
INSERT INTO demande_service (id_demande, id_service, quantite) VALUES
    (1, 1, 1),
    (1, 5, 1);

-- Demande 2 : Bob veut Révision
INSERT INTO demande_service (id_demande, id_service, quantite) VALUES
    (2, 2, 1);

-- Demande 3 : Claire veut Changement pneus x4
INSERT INTO demande_service (id_demande, id_service, quantite) VALUES
    (3, 4, 4);

-- Demande 5 : Eva voulait Diagnostic
INSERT INTO demande_service (id_demande, id_service, quantite) VALUES
    (5, 5, 1);


-- --------------------------------------------------
-- Devis
-- --------------------------------------------------

INSERT INTO devis (id_devis, id_demande, date_devis, montant_total) VALUES
    (1, 1, '2025-06-21 14:00:00', 59.90 + 79.00),  -- Alice
    (2, 2, '2025-06-20 15:00:00', 129.90);        -- Bob


-- --------------------------------------------------
-- RendezVous
-- --------------------------------------------------

INSERT INTO rendez_vous (id_rdv, id_demande, id_admin, id_creneau, code_statut) VALUES
    (1, 3, 1, 1, 'Confirme'),   -- Claire sur créneau 1
    (2, 4, 2, 3, 'Reporte'),    -- David déplacé sur créneau 3
    (3, 6, 1, 6, 'Annule');     -- Alice annulé, créneau 6


