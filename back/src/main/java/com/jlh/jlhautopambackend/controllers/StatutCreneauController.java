package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.modeles.StatutCreneau;
import com.jlh.jlhautopambackend.repositories.StatutCreneauRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/statuts-creneau")
public class StatutCreneauController {

    private final StatutCreneauRepository statutRepo;

    public StatutCreneauController(StatutCreneauRepository statutRepo) {
        this.statutRepo = statutRepo;
    }

    // GET /api/statuts-creneau
    @GetMapping
    public List<StatutCreneau> getAll() {
        return statutRepo.findAll();
    }

    // GET /api/statuts-creneau/{code}
    @GetMapping("/{code}")
    public ResponseEntity<StatutCreneau> getById(@PathVariable String code) {
        return statutRepo.findById(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/statuts-creneau
    @PostMapping
    public ResponseEntity<StatutCreneau> create(@Valid @RequestBody StatutCreneau statut) {
        if (statutRepo.existsById(statut.getCodeStatut())) {
            // Conflict : ce code existe déjà
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        StatutCreneau saved = statutRepo.save(statut);
        return ResponseEntity
                .created(URI.create("/api/statuts-creneau/" + saved.getCodeStatut()))
                .body(saved);
    }

    // PUT /api/statuts-creneau/{code}
    @PutMapping("/{code}")
    public ResponseEntity<StatutCreneau> update(
            @PathVariable String code,
            @Valid @RequestBody StatutCreneau dto
    ) {
        return statutRepo.findById(code).map(existing -> {
            existing.setLibelle(dto.getLibelle());
            StatutCreneau updated = statutRepo.save(existing);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/statuts-creneau/{code}
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        if (!statutRepo.existsById(code)) {
            return ResponseEntity.notFound().build();
        }
        statutRepo.deleteById(code);
        return ResponseEntity.noContent().build();
    }
}
