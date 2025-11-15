package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.modeles.StatutRendezVous;
import com.jlh.jlhautopambackend.repositories.StatutRendezVousRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/statuts-rendezvous")
public class StatutRendezVousController {

    private final StatutRendezVousRepository statutRepo;

    public StatutRendezVousController(StatutRendezVousRepository statutRepo) {
        this.statutRepo = statutRepo;
    }

    // GET /api/statuts-rendezvous
    @GetMapping
    public List<StatutRendezVous> getAll() {
        return statutRepo.findAll();
    }

    // GET /api/statuts-rendezvous/{code}
    @GetMapping("/{code}")
    public ResponseEntity<StatutRendezVous> getByCode(@PathVariable String code) {
        return statutRepo.findById(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/statuts-rendezvous
    @PostMapping
    public ResponseEntity<StatutRendezVous> create(@Valid @RequestBody StatutRendezVous dto) {
        if (statutRepo.existsById(dto.getCodeStatut())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        StatutRendezVous saved = statutRepo.save(dto);
        return ResponseEntity
                .created(URI.create("/api/statuts-rendezvous/" + saved.getCodeStatut()))
                .body(saved);
    }

    // PUT /api/statuts-rendezvous/{code}
    @PutMapping("/{code}")
    public ResponseEntity<StatutRendezVous> update(
            @PathVariable String code,
            @Valid @RequestBody StatutRendezVous dto
    ) {
        return statutRepo.findById(code).map(existing -> {
            existing.setLibelle(dto.getLibelle());
            StatutRendezVous updated = statutRepo.save(existing);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/statuts-rendezvous/{code}
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        if (!statutRepo.existsById(code)) {
            return ResponseEntity.notFound().build();
        }
        statutRepo.deleteById(code);
        return ResponseEntity.noContent().build();
    }
}
