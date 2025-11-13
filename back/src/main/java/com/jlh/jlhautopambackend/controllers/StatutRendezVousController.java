package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.StatutRendezVousDto;
import com.jlh.jlhautopambackend.services.StatutRendezVousService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/statut-rendezvous")
public class StatutRendezVousController {

    private final StatutRendezVousService service;

    public StatutRendezVousController(StatutRendezVousService service) {
        this.service = service;
    }

    @GetMapping
    public List<StatutRendezVousDto> getAll() {
        return service.findAll();
    }

    @GetMapping("/{code}")
    public ResponseEntity<StatutRendezVousDto> getByCode(@PathVariable String code) {
        return service.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StatutRendezVousDto> create(
            @Valid @RequestBody StatutRendezVousDto dto) {
        StatutRendezVousDto created = service.create(dto);
        return ResponseEntity
                .created(URI.create("/api/statut-rendezvous/" + created.getCodeStatut()))
                .body(created);
    }

    @PutMapping("/{code}")
    public ResponseEntity<StatutRendezVousDto> update(
            @PathVariable String code,
            @Valid @RequestBody StatutRendezVousDto dto) {
        return service.update(code, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        return service.delete(code)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
