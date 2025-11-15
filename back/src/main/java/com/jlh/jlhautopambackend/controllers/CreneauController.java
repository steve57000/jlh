package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.modeles.Creneau;
import com.jlh.jlhautopambackend.modeles.StatutCreneau;
import com.jlh.jlhautopambackend.repositories.CreneauRepository;
import com.jlh.jlhautopambackend.repositories.StatutCreneauRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/api/creneaux")
public class CreneauController {

    private final CreneauRepository creneauRepo;
    private final StatutCreneauRepository statutRepo;

    public CreneauController(CreneauRepository creneauRepo,
                             StatutCreneauRepository statutRepo) {
        this.creneauRepo = creneauRepo;
        this.statutRepo = statutRepo;
    }

    // GET /api/creneaux
    @GetMapping
    public List<Creneau> getAll() {
        return creneauRepo.findAll();
    }

    // GET /api/creneaux/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Creneau> getById(@PathVariable Integer id) {
        return creneauRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/creneaux
    @PostMapping
    public ResponseEntity<Creneau> create(@Valid @RequestBody Creneau creneau) {
        // Vérifier que le StatutCreneau référencé existe
        String statutId = creneau.getStatut().getCodeStatut();
        Optional<StatutCreneau> maybeStatut = statutRepo.findById(statutId);
        if (maybeStatut.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        creneau.setStatut(maybeStatut.get());

        // Sauvegarde (cascade sur disponibilites)
        Creneau saved = creneauRepo.save(creneau);
        return ResponseEntity
                .created(URI.create("/api/creneaux/" + saved.getIdCreneau()))
                .body(saved);
    }

    // PUT /api/creneaux/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Creneau> update(
            @PathVariable Integer id,
            @Valid @RequestBody Creneau dto
    ) {
        return creneauRepo.findById(id)
                .map(existing -> {
                    existing.setDateDebut(dto.getDateDebut());
                    existing.setDateFin(dto.getDateFin());

                    // Mettre à jour le statut
                    String newStatutId = dto.getStatut().getCodeStatut();
                    Optional<StatutCreneau> maybeNewStatut = statutRepo.findById(newStatutId);
                    if (maybeNewStatut.isEmpty()) {
                        return ResponseEntity.badRequest().<Creneau>build();
                    }
                    existing.setStatut(maybeNewStatut.get());

                    // Remplacer la liste des disponibilités
                    existing.getDisponibilites().clear();
                    existing.getDisponibilites().addAll(dto.getDisponibilites());

                    Creneau updated = creneauRepo.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/creneaux/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!creneauRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        creneauRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
