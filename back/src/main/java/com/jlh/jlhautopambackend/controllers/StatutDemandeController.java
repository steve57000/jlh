package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.StatutDemandeDto;
import com.jlh.jlhautopambackend.services.StatutDemandeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/statut-demandes")
public class StatutDemandeController {
    private final StatutDemandeService service;

    public StatutDemandeController(StatutDemandeService service) {
        this.service = service;
    }

    @GetMapping
    public List<StatutDemandeDto> getAll() {
        return service.findAll();
    }

    @GetMapping("/{code}")
    public ResponseEntity<StatutDemandeDto> getByCode(@PathVariable String code) {
        return service.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StatutDemandeDto> create(
            @Valid @RequestBody StatutDemandeDto dto) {
        StatutDemandeDto created = service.create(dto);
        String path = String.format("/api/statut-demandes/%s", created.getCodeStatut());
        return ResponseEntity.created(URI.create(path)).body(created);
    }

    @PutMapping("/{code}")
    public ResponseEntity<StatutDemandeDto> update(
            @PathVariable String code,
            @Valid @RequestBody StatutDemandeDto dto) {
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

