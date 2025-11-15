package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.modeles.*;
import com.jlh.jlhautopambackend.repositories.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/api/rendezvous")
public class RendezVousController {

    private final RendezVousRepository rvRepo;
    private final DemandeRepository demandeRepo;
    private final AdministrateurRepository adminRepo;
    private final CreneauRepository creneauRepo;
    private final StatutRendezVousRepository statutRepo;

    public RendezVousController(
        RendezVousRepository rvRepo,
        DemandeRepository demandeRepo,
        AdministrateurRepository adminRepo,
        CreneauRepository creneauRepo,
        StatutRendezVousRepository statutRepo)
        {
            this.rvRepo = rvRepo;
            this.demandeRepo = demandeRepo;
            this.adminRepo = adminRepo;
            this.creneauRepo = creneauRepo;
            this.statutRepo = statutRepo;
        }

    // LISTE TOUS LES RDV
    @GetMapping
    public List<RendezVous> getAll() {
        return rvRepo.findAll();
    }

    // RÉCUPÈRE UN RDV PAR ID
    @GetMapping("/{id}")
    public ResponseEntity<RendezVous> getById(@PathVariable Integer id) {
        return rvRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CRÉE UN NOUVEAU RDV
    @PostMapping
    public ResponseEntity<RendezVous> create(@Valid @RequestBody RendezVous dto) {
        Integer demandeId   = dto.getDemande().getIdDemande();
        Integer adminId     = dto.getAdministrateur().getIdAdmin();
        Integer creneauId   = dto.getCreneau().getIdCreneau();
        String statutCode   = dto.getStatut().getCodeStatut();

        // Charger et vérifier existence de chaque entité liée
        Optional<Demande> optDemande = demandeRepo.findById(demandeId);
        Optional<Administrateur> optAdmin = adminRepo.findById(adminId);
        Optional<Creneau> optCreneau = creneauRepo.findById(creneauId);
        Optional<StatutRendezVous> optStatut = statutRepo.findById(statutCode);

        if (optDemande.isEmpty() || optAdmin.isEmpty() ||
                optCreneau.isEmpty() || optStatut.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Contrainte one-to-one unique
        if (rvRepo.existsByDemandeIdDemande(demandeId) ||
                rvRepo.existsByCreneauIdCreneau(creneauId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Tous les Optionals sont présents : on peut safely .get()
        RendezVous toSave = RendezVous.builder()
                .demande(optDemande.get())
                .administrateur(optAdmin.get())
                .creneau(optCreneau.get())
                .statut(optStatut.get())
                .build();

        RendezVous saved = rvRepo.save(toSave);
        return ResponseEntity
                .created(URI.create("/api/rendezvous/" + saved.getIdRdv()))
                .body(saved);
    }

    // MET À JOUR UN RDV EXISTANT
    @PutMapping("/{id}")
    public ResponseEntity<RendezVous> update(
            @PathVariable Integer id,
            @Valid @RequestBody RendezVous dto
    ) {
        return rvRepo.findById(id).map(existing -> {
            // -> Demande
            Integer newDemandeId = dto.getDemande().getIdDemande();
            if (!existing.getDemande().getIdDemande().equals(newDemandeId)) {
                Optional<Demande> optNewDemande = demandeRepo.findById(newDemandeId);
                if (optNewDemande.isEmpty() ||
                        rvRepo.existsByDemandeIdDemande(newDemandeId)) {
                    return ResponseEntity.badRequest().<RendezVous>build();
                }
                existing.setDemande(optNewDemande.get());
            }
            // -> Administrateur
            Integer newAdminId = dto.getAdministrateur().getIdAdmin();
            if (!existing.getAdministrateur().getIdAdmin().equals(newAdminId)) {
                Optional<Administrateur> optNewAdmin = adminRepo.findById(newAdminId);
                if (optNewAdmin.isEmpty()) {
                    return ResponseEntity.badRequest().<RendezVous>build();
                }
                existing.setAdministrateur(optNewAdmin.get());
            }
            // -> Créneau
            Integer newCreneauId = dto.getCreneau().getIdCreneau();
            if (!existing.getCreneau().getIdCreneau().equals(newCreneauId)) {
                Optional<Creneau> optNewCreneau = creneauRepo.findById(newCreneauId);
                if (optNewCreneau.isEmpty() ||
                        rvRepo.existsByCreneauIdCreneau(newCreneauId)) {
                    return ResponseEntity.badRequest().<RendezVous>build();
                }
                existing.setCreneau(optNewCreneau.get());
            }
            // -> Statut
            String newStatutCode = dto.getStatut().getCodeStatut();
            if (!existing.getStatut().getCodeStatut().equals(newStatutCode)) {
                Optional<StatutRendezVous> optNewStatut = statutRepo.findById(newStatutCode);
                if (optNewStatut.isEmpty()) {
                    return ResponseEntity.badRequest().<RendezVous>build();
                }
                existing.setStatut(optNewStatut.get());
            }

            RendezVous updated = rvRepo.save(existing);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // SUPPRIME UN RDV
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!rvRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        rvRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
