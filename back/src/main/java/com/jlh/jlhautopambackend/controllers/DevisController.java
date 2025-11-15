package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.modeles.Devis;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.repositories.DevisRepository;
import com.jlh.jlhautopambackend.repositories.DemandeRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/api/devis")
public class DevisController {

    private final DevisRepository devisRepo;
    private final DemandeRepository demandeRepo;

    public DevisController(DevisRepository devisRepo,
                           DemandeRepository demandeRepo) {
        this.devisRepo = devisRepo;
        this.demandeRepo = demandeRepo;
    }

    // GET /api/devis
    @GetMapping
    public List<Devis> getAll() {
        return devisRepo.findAll();
    }

    // GET /api/devis/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Devis> getById(@PathVariable Integer id) {
        return devisRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/devis
    @PostMapping
    public ResponseEntity<Devis> create(@Valid @RequestBody Devis dto) {
        Integer demandeId = dto.getDemande().getIdDemande();
        Optional<Demande> maybeDemande = demandeRepo.findById(demandeId);
        if (maybeDemande.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        // Vérifier qu'aucun devis n'existe déjà pour cette demande (unicité one-to-one)
        if (devisRepo.findAll().stream()
                .anyMatch(d -> d.getDemande().getIdDemande().equals(demandeId))) {
            return ResponseEntity.status(409).build(); // Conflict
        }

        dto.setDemande(maybeDemande.get());
        Devis saved = devisRepo.save(dto);
        return ResponseEntity
                .created(URI.create("/api/devis/" + saved.getIdDevis()))
                .body(saved);
    }

    // PUT /api/devis/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Devis> update(
            @PathVariable Integer id,
            @Valid @RequestBody Devis dto
    ) {
        return devisRepo.findById(id).map(existing -> {
            // Si on change la demande associée, vérifier son existence et l'unicité
            Integer newDemandeId = dto.getDemande().getIdDemande();
            if (!existing.getDemande().getIdDemande().equals(newDemandeId)) {
                Optional<Demande> maybeNewDemande = demandeRepo.findById(newDemandeId);
                if (maybeNewDemande.isEmpty()) {
                    return ResponseEntity.badRequest().<Devis>build();
                }
                boolean conflict = devisRepo.findAll().stream()
                        .anyMatch(d -> d.getDemande().getIdDemande().equals(newDemandeId));
                if (conflict) {
                    return ResponseEntity.status(409).<Devis>build();
                }
                existing.setDemande(maybeNewDemande.get());
            }
            existing.setDateDevis(dto.getDateDevis());
            existing.setMontantTotal(dto.getMontantTotal());
            Devis updated = devisRepo.save(existing);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/devis/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!devisRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        devisRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
