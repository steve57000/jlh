package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.TypeDemandeDto;
import com.jlh.jlhautopambackend.services.TypeDemandeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/types-demande")
public class TypeDemandeController {

    private final TypeDemandeService service;

    public TypeDemandeController(TypeDemandeService service) {
        this.service = service;
    }

    @GetMapping
    public List<TypeDemandeDto> getAll() {
        return service.findAll();
    }

    @GetMapping("/{code}")
    public ResponseEntity<TypeDemandeDto> getByCode(@PathVariable String code) {
        return service.findById(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TypeDemandeDto> create(
            @Valid @RequestBody TypeDemandeDto dto) {
        try {
            TypeDemandeDto created = service.create(dto);
            URI uri = URI.create("/api/types-demande/" + created.getCodeType());
            return ResponseEntity.created(uri).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @PutMapping("/{code}")
    public ResponseEntity<TypeDemandeDto> update(
            @PathVariable String code,
            @Valid @RequestBody TypeDemandeDto dto) {
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
