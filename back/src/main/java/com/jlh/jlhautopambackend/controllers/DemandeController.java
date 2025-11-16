package com.jlh.jlhautopambackend.controllers;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.jlh.jlhautopambackend.dto.DemandeDto;
import com.jlh.jlhautopambackend.mapper.DemandeMapper;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.repositories.DemandeRepository;

@CrossOrigin
@RestController
@RequestMapping("/api/demandes")
public class DemandeController {

    private final DemandeRepository repo;
    private final DemandeMapper mapper;

    public DemandeController(DemandeRepository repo, DemandeMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @GetMapping
    public List<DemandeDto> getAll() {
        return mapper.toDtos(repo.findAll());
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public ResponseEntity<DemandeDto> getById(@PathVariable Integer id) {
        return repo.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DemandeDto> create(@RequestBody Demande d) {
        Demande saved = repo.save(d);
        return ResponseEntity.status(201).body(mapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DemandeDto> update(@PathVariable Integer id,
                                             @RequestBody Demande input) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setDateSoumission(input.getDateSoumission());
                    existing.setClient(input.getClient());
                    existing.setTypeDemande(input.getTypeDemande());
                    existing.setStatutDemande(input.getStatutDemande());
                    existing.setServices(input.getServices());
                    Demande updated = repo.save(existing);
                    return ResponseEntity.ok(mapper.toDto(updated));
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
