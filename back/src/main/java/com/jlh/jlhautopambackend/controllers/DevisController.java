package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.DevisRequest;
import com.jlh.jlhautopambackend.dto.DevisResponse;
import com.jlh.jlhautopambackend.dto.RendezVousRequest;
import com.jlh.jlhautopambackend.dto.RendezVousResponse;
import com.jlh.jlhautopambackend.repository.AdministrateurRepository;
import com.jlh.jlhautopambackend.services.RendezVousService;
import com.jlh.jlhautopambackend.services.support.AuthenticatedClientResolver;
import com.jlh.jlhautopambackend.services.DevisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/devis")
public class DevisController {

    private final DevisService service;
    private final RendezVousService rendezVousService;
    private final AuthenticatedClientResolver clientResolver;
    private final AdministrateurRepository adminRepository;

    public DevisController(DevisService service,
                           RendezVousService rendezVousService,
                           AuthenticatedClientResolver clientResolver,
                           AdministrateurRepository adminRepository) {
        this.service = service;
        this.rendezVousService = rendezVousService;
        this.clientResolver = clientResolver;
        this.adminRepository = adminRepository;
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
        RendezVousResponse resp = rendezVousService.createForDevis(id, req, clientId);
        return ResponseEntity
                .created(URI.create("/api/rendezvous/" + resp.getIdRdv()))
                .body(resp);
    }
}
