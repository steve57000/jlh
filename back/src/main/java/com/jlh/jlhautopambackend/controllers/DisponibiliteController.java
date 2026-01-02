package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.modeles.Disponibilite;
import com.jlh.jlhautopambackend.modeles.DisponibiliteKey;
import com.jlh.jlhautopambackend.services.DisponibiliteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/disponibilites")
public class DisponibiliteController {

    private final DisponibiliteService service;

    public DisponibiliteController(DisponibiliteService service) {
        this.service = service;
    }

    @GetMapping
    public List<Disponibilite> getAll() {
        return service.findAll();
    }

    @GetMapping("/{adminId}/{creneauId}")
    public ResponseEntity<Disponibilite> getById(@PathVariable Integer adminId,
                                                 @PathVariable Integer creneauId) {
        return service.findByKey(adminId, creneauId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Disponibilite> create(@Valid @RequestBody Disponibilite dto) {
        try {
            Disponibilite saved = service.create(dto);
            DisponibiliteKey k = saved.getId();
            String path = String.format("/api/disponibilites/%d/%d",
                    k.getIdAdmin(), k.getIdCreneau());
            return ResponseEntity.created(URI.create(path)).body(saved);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{adminId}/{creneauId}")
    public ResponseEntity<Void> delete(@PathVariable Integer adminId,
                                       @PathVariable Integer creneauId) {
        return service.delete(adminId, creneauId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
