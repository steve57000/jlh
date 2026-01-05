package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.ServiceIconRequest;
import com.jlh.jlhautopambackend.dto.ServiceIconResponse;
import com.jlh.jlhautopambackend.services.ServiceIconService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/service-icons")
public class ServiceIconController {
    private final ServiceIconService service;

    public ServiceIconController(ServiceIconService service) {
        this.service = service;
    }

    @GetMapping
    public List<ServiceIconResponse> getAll() {
        return service.findAll();
    }

    @PostMapping
    public ResponseEntity<ServiceIconResponse> create(@Valid @RequestBody ServiceIconRequest request) {
        return service.create(request)
                .map(saved -> ResponseEntity
                        .created(URI.create("/api/service-icons/" + saved.getIdIcon()))
                        .body(saved))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
