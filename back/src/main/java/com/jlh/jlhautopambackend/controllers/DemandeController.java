package com.jlh.jlhautopambackend.controllers;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.repositories.DemandeRepository;

@CrossOrigin
@RestController
@RequestMapping("/api/demandes")
public class DemandeController {

    private final DemandeRepository repo;

    public DemandeController(DemandeRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Demande> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Demande> getById(@PathVariable Integer id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Demande> create(@RequestBody Demande d) {
        Demande saved = repo.save(d);
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Demande> update(@PathVariable Integer id,
                                          @RequestBody Demande input) {
        return repo.findById(id)
                .map(existing -> {
                    // votre entité utilise Instant pour dateSoumission
                    existing.setDateSoumission(input.getDateSoumission());
                    existing.setClient(input.getClient());
                    existing.setTypeDemande(input.getTypeDemande());
                    existing.setStatutDemande(input.getStatutDemande());
                    existing.setServices(input.getServices());
                    return ResponseEntity.ok(repo.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return repo.findById(id)
                .map(e -> {
                    repo.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
