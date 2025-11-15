package com.jlh.jlhautopambackend.controllers;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.jlh.jlhautopambackend.modeles.Service;
import com.jlh.jlhautopambackend.repositories.ServiceRepository;
import jakarta.validation.Valid;

@CrossOrigin
@RestController
@RequestMapping("/api/services")
public class ServiceController {
    private final ServiceRepository repo;
    public ServiceController(ServiceRepository repo) { this.repo = repo; }
    @GetMapping public List<Service> getAll() { return repo.findAll(); }
    @GetMapping("/{id}") public ResponseEntity<Service> getById(@PathVariable Integer id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping public Service create(@Valid @RequestBody Service s) { return repo.save(s); }
    @PutMapping("/{id}") public ResponseEntity<Service> update(@PathVariable Integer id,
                                                               @Valid @RequestBody Service input) {
        return repo.findById(id)
                .map(e -> {
                    e.setLibelle(input.getLibelle());
                    e.setDescription(input.getDescription());
                    e.setPrixUnitaire(input.getPrixUnitaire());
                    return ResponseEntity.ok(repo.save(e));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return repo.findById(id)
                .map(e -> { repo.deleteById(id); return ResponseEntity.noContent().<Void>build(); })
                .orElse(ResponseEntity.notFound().build());
    }
}
