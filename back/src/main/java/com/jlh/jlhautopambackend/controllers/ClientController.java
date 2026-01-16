package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.ClientRequest;
import com.jlh.jlhautopambackend.dto.ClientResponse;
import com.jlh.jlhautopambackend.dto.ClientUpdateRequest;
import com.jlh.jlhautopambackend.services.ClientService;
import com.jlh.jlhautopambackend.services.RgpdAnonymizationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService service;
    private final RgpdAnonymizationService anonymizationService;

    public ClientController(ClientService service, RgpdAnonymizationService anonymizationService) {
        this.service = service;
        this.anonymizationService = anonymizationService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<ClientResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ClientResponse> getById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ClientResponse> create(
            @Valid @RequestBody ClientRequest request) {
        ClientResponse created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/clients/" + created.getIdClient()))
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ClientResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody ClientUpdateRequest request) {
        return service.update(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/anonymize")
    @PreAuthorize("hasRole('ADMIN_PRINCIPAL')")
    public ResponseEntity<?> anonymize(@PathVariable Integer id) {
        return anonymizationService.anonymizeClient(id, "ADMIN_PRINCIPAL")
                .map(client -> ResponseEntity.ok(Map.of(
                        "idClient", client.getIdClient(),
                        "anonymizedAt", client.getAnonymizedAt() != null ? client.getAnonymizedAt() : Instant.now()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
