package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.CreneauRequest;
import com.jlh.jlhautopambackend.dto.CreneauResponse;
import com.jlh.jlhautopambackend.services.CreneauService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/creneaux")
public class CreneauController {

    private final CreneauService service;

    public CreneauController(CreneauService service) {
        this.service = service;
    }

    @GetMapping
    public List<CreneauResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreneauResponse> getById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CreneauResponse> create(
            @Valid @RequestBody CreneauRequest request) {
        CreneauResponse created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/creneaux/" + created.getIdCreneau()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreneauResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody CreneauRequest request) {
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
