package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.repository.AdministrateurRepository;
import com.jlh.jlhautopambackend.services.RendezVousService;
import com.jlh.jlhautopambackend.services.support.AuthenticatedClientResolver;
import com.jlh.jlhautopambackend.services.ServiceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceService service;
    private final RendezVousService rendezVousService;
    private final AuthenticatedClientResolver clientResolver;
    private final AdministrateurRepository adminRepository;

    public ServiceController(ServiceService service,
                             RendezVousService rendezVousService,
                             AuthenticatedClientResolver clientResolver,
                             AdministrateurRepository adminRepository) {
        this.service = service;
        this.rendezVousService = rendezVousService;
        this.clientResolver = clientResolver;
        this.adminRepository = adminRepository;
    }

    @GetMapping
    public List<ServiceResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> create(
            @Valid @RequestBody ServiceRequest req) {
        ServiceResponse created = service.create(req);
        return ResponseEntity
                .created(URI.create("/api/services/" + created.getIdService()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody ServiceRequest req) {
        return service.update(id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/rendezvous")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<RendezVousResponse> createRendezVous(
            @PathVariable Integer id,
            @Valid @RequestBody RendezVousRequest req,
            Authentication auth) {
        Integer clientId = null;
        boolean isClient = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));
        if (isClient) {
            clientId = clientResolver.requireCurrentClient(auth).getIdClient();
        } else if (req.getAdministrateurId() == null && auth != null) {
            adminRepository.findByEmail(auth.getName())
                    .ifPresent(admin -> req.setAdministrateurId(admin.getIdAdmin()));
        }
        RendezVousResponse resp = rendezVousService.createForService(id, req, clientId);
        return ResponseEntity
                .created(URI.create("/api/rendezvous/" + resp.getIdRdv()))
                .body(resp);
    }
}
