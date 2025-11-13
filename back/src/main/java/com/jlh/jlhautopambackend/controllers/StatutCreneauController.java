package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.StatutCreneauDto;
import com.jlh.jlhautopambackend.services.StatutCreneauService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/statuts-creneau")
public class StatutCreneauController {
    private final StatutCreneauService service;

    public StatutCreneauController(StatutCreneauService service) {
        this.service = service;
    }

    @GetMapping
    public List<StatutCreneauDto> getAll() {
        return service.findAll();
    }

    @GetMapping("/{code}")
    public ResponseEntity<StatutCreneauDto> getByCode(@PathVariable String code) {
        return service.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StatutCreneauDto> create(@Valid @RequestBody StatutCreneauDto dto) {
        try {
            StatutCreneauDto created = service.create(dto);
            return ResponseEntity
                    .created(URI.create("/api/statuts-creneau/" + created.getCodeStatut()))
                    .body(created);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{code}")
    public ResponseEntity<StatutCreneauDto> update(
            @PathVariable String code,
            @Valid @RequestBody StatutCreneauDto dto
    ) {
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
