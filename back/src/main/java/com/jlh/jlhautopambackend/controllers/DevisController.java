package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.DevisRequest;
import com.jlh.jlhautopambackend.dto.DevisResponse;
import com.jlh.jlhautopambackend.services.DevisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/devis")
public class DevisController {

    private final DevisService service;

    public DevisController(DevisService service) {
        this.service = service;
    }

    @GetMapping
    public List<DevisResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DevisResponse> getById(@PathVariable Integer id) {
        Optional<DevisResponse> optional = service.findById(id);
        return optional
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DevisResponse> create(
            @Valid @RequestBody DevisRequest request) {
        try {
            DevisResponse resp = service.create(request);
            URI location = URI.create("/api/devis/" + resp.getIdDevis());
            return ResponseEntity.created(location).body(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<DevisResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody DevisRequest request) {
        try {
            Optional<DevisResponse> optional = service.update(id, request);
            return optional
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
