package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.AdministrateurRequest;
import com.jlh.jlhautopambackend.dto.AdministrateurResponse;
import com.jlh.jlhautopambackend.services.AdministrateurService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/administrateurs")
public class AdministrateurController {

    private final AdministrateurService service;

    public AdministrateurController(AdministrateurService service) {
        this.service = service;
    }

    @GetMapping
    public List<AdministrateurResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdministrateurResponse> getById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AdministrateurResponse> create(
            @Valid @RequestBody AdministrateurRequest request) {
        AdministrateurResponse created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/administrateurs/" + created.getIdAdmin()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdministrateurResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody AdministrateurRequest request) {
        return service.update(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
