package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.modeles.StatutDemande;
import com.jlh.jlhautopambackend.repositories.StatutDemandeRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/statuts-demande")
public class StatutDemandeController {

    private final StatutDemandeRepository statutRepo;

    public StatutDemandeController(StatutDemandeRepository statutRepo) {
        this.statutRepo = statutRepo;
    }

    // GET /api/statuts-demande
    @GetMapping
    public List<StatutDemande> getAll() {
        return statutRepo.findAll();
    }

    // GET /api/statuts-demande/{code}
    @GetMapping("/{code}")
    public ResponseEntity<StatutDemande> getByCode(@PathVariable String code) {
        return statutRepo.findById(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/statuts-demande
    @PostMapping
    public ResponseEntity<StatutDemande> create(@Valid @RequestBody StatutDemande statut) {
        if (statutRepo.existsById(statut.getCodeStatut())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        StatutDemande saved = statutRepo.save(statut);
        return ResponseEntity
                .created(URI.create("/api/statuts-demande/" + saved.getCodeStatut()))
                .body(saved);
    }

    // PUT /api/statuts-demande/{code}
    @PutMapping("/{code}")
    public ResponseEntity<StatutDemande> update(
            @PathVariable String code,
            @Valid @RequestBody StatutDemande dto
    ) {
        return statutRepo.findById(code).map(existing -> {
            existing.setLibelle(dto.getLibelle());
            StatutDemande updated = statutRepo.save(existing);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/statuts-demande/{code}
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        if (!statutRepo.existsById(code)) {
            return ResponseEntity.notFound().build();
        }
        statutRepo.deleteById(code);
        return ResponseEntity.noContent().build();
    }
}
