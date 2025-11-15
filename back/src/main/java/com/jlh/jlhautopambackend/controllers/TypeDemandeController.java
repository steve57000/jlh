package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.modeles.TypeDemande;
import com.jlh.jlhautopambackend.repositories.TypeDemandeRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/types-demande")
public class TypeDemandeController {

    private final TypeDemandeRepository typeRepo;

    public TypeDemandeController(TypeDemandeRepository typeRepo) {
        this.typeRepo = typeRepo;
    }

    // GET /api/types-demande
    @GetMapping
    public List<TypeDemande> getAll() {
        return typeRepo.findAll();
    }

    // GET /api/types-demande/{code}
    @GetMapping("/{code}")
    public ResponseEntity<TypeDemande> getByCode(@PathVariable String code) {
        return typeRepo.findById(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/types-demande
    @PostMapping
    public ResponseEntity<TypeDemande> create(@Valid @RequestBody TypeDemande dto) {
        if (typeRepo.existsById(dto.getCodeType())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        TypeDemande saved = typeRepo.save(dto);
        return ResponseEntity
                .created(URI.create("/api/types-demande/" + saved.getCodeType()))
                .body(saved);
    }

    // PUT /api/types-demande/{code}
    @PutMapping("/{code}")
    public ResponseEntity<TypeDemande> update(
            @PathVariable String code,
            @Valid @RequestBody TypeDemande dto
    ) {
        return typeRepo.findById(code).map(existing -> {
            existing.setLibelle(dto.getLibelle());
            TypeDemande updated = typeRepo.save(existing);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/types-demande/{code}
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        if (!typeRepo.existsById(code)) {
            return ResponseEntity.notFound().build();
        }
        typeRepo.deleteById(code);
        return ResponseEntity.noContent().build();
    }
}
