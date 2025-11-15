package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.repositories.AdministrateurRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/administrateurs")
public class AdministrateurController {

    private final AdministrateurRepository administrateurRepo;

    public AdministrateurController(AdministrateurRepository administrateurRepo) {
        this.administrateurRepo = administrateurRepo;
    }

    // GET /api/administrateurs
    @GetMapping
    public List<Administrateur> getAll() {
        return administrateurRepo.findAll();
    }

    // GET /api/administrateurs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Administrateur> getById(@PathVariable Integer id) {
        return administrateurRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/administrateurs
    @PostMapping
    public ResponseEntity<Administrateur> create(@Valid @RequestBody Administrateur admin) {
        Administrateur saved = administrateurRepo.save(admin);
        return ResponseEntity
                .created(URI.create("/api/administrateurs/" + saved.getIdAdmin()))
                .body(saved);
    }

    // PUT /api/administrateurs/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Administrateur> update(
            @PathVariable Integer id,
            @Valid @RequestBody Administrateur adminDetails
    ) {
        return administrateurRepo.findById(id)
                .map(existing -> {
                    existing.setUsername(adminDetails.getUsername());
                    existing.setMotDePasse(adminDetails.getMotDePasse());
                    existing.setNom(adminDetails.getNom());
                    existing.setPrenom(adminDetails.getPrenom());
                    // Gérer les disponibilités si besoin
                    existing.getDisponibilites().clear();
                    existing.getDisponibilites().addAll(adminDetails.getDisponibilites());
                    Administrateur updated = administrateurRepo.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/administrateurs/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!administrateurRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        administrateurRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
