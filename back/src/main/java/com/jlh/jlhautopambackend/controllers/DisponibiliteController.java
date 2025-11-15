package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.modeles.Disponibilite;
import com.jlh.jlhautopambackend.modeles.DisponibiliteKey;
import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.modeles.Creneau;
import com.jlh.jlhautopambackend.repositories.DisponibiliteRepository;
import com.jlh.jlhautopambackend.repositories.AdministrateurRepository;
import com.jlh.jlhautopambackend.repositories.CreneauRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/disponibilites")
public class DisponibiliteController {

    private final DisponibiliteRepository dispoRepo;
    private final AdministrateurRepository adminRepo;
    private final CreneauRepository creneauRepo;

    public DisponibiliteController(DisponibiliteRepository dispoRepo,
                                   AdministrateurRepository adminRepo,
                                   CreneauRepository creneauRepo) {
        this.dispoRepo = dispoRepo;
        this.adminRepo = adminRepo;
        this.creneauRepo = creneauRepo;
    }

    // GET all
    @GetMapping
    public List<Disponibilite> getAll() {
        return dispoRepo.findAll();
    }

    // GET by composite key
    @GetMapping("/{adminId}/{creneauId}")
    public ResponseEntity<Disponibilite> getById(@PathVariable Integer adminId,
                                                 @PathVariable Integer creneauId) {
        DisponibiliteKey key = new DisponibiliteKey(adminId, creneauId);
        return dispoRepo.findById(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST create
    @PostMapping
    public ResponseEntity<Disponibilite> create(@Valid @RequestBody Disponibilite dto) {
        Integer adminId = dto.getAdministrateur().getIdAdmin();
        Integer creneauId = dto.getCreneau().getIdCreneau();

        return adminRepo.findById(adminId).flatMap(admin ->
                creneauRepo.findById(creneauId).map(creneau -> {
                    DisponibiliteKey key = new DisponibiliteKey(adminId, creneauId);
                    Disponibilite dispo = Disponibilite.builder()
                            .id(key)
                            .administrateur(admin)
                            .creneau(creneau)
                            .build();
                    Disponibilite saved = dispoRepo.save(dispo);
                    return ResponseEntity
                            .created(URI.create("/api/disponibilites/" + adminId + "/" + creneauId))
                            .body(saved);
                })
        ).orElseGet(() -> ResponseEntity.badRequest().build());
    }

    // DELETE by composite key
    @DeleteMapping("/{adminId}/{creneauId}")
    public ResponseEntity<Void> delete(@PathVariable Integer adminId,
                                       @PathVariable Integer creneauId) {
        DisponibiliteKey key = new DisponibiliteKey(adminId, creneauId);
        if (!dispoRepo.existsById(key)) {
            return ResponseEntity.notFound().build();
        }
        dispoRepo.deleteById(key);
        return ResponseEntity.noContent().build();
    }
}
