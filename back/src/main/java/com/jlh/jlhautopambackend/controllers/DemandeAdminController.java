package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.DemandeDto;
import com.jlh.jlhautopambackend.mapper.DemandeMapper;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/admin/demandes")
@PreAuthorize("hasRole('ADMIN')")
public class DemandeAdminController {

    private final DemandeRepository repository;
    private final DemandeMapper mapper;

    public DemandeAdminController(DemandeRepository repository, DemandeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @GetMapping
    public List<DemandeDto> getAll() {
        return mapper.toDtos(repository.findAll());
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public ResponseEntity<DemandeDto> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DemandeDto> create(@RequestBody Demande demande) {
        Demande saved = repository.save(demande);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DemandeDto> update(@PathVariable Integer id, @RequestBody Demande payload) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setDateDemande(payload.getDateDemande());
                    existing.setClient(payload.getClient());
                    existing.setTypeDemande(payload.getTypeDemande());
                    existing.setStatutDemande(payload.getStatutDemande());
                    existing.setServices(payload.getServices());
                    Demande updated = repository.save(existing);
                    return ResponseEntity.ok(mapper.toDto(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return repository.findById(id)
                .map(entity -> {
                    repository.delete(entity);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
