package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.RendezVousRequest;
import com.jlh.jlhautopambackend.dto.RendezVousResponse;
import com.jlh.jlhautopambackend.services.RendezVousService;
import com.jlh.jlhautopambackend.services.support.AuthenticatedClientResolver;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/rendezvous")
public class RendezVousController {

    private final RendezVousService service;
    private final AuthenticatedClientResolver clientResolver;

    public RendezVousController(RendezVousService service, AuthenticatedClientResolver clientResolver) {
        this.service = service;
        this.clientResolver = clientResolver;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')") // ajuste si besoin
    public List<RendezVousResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<RendezVousResponse> getById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<RendezVousResponse> create(@Valid @RequestBody RendezVousRequest req) {
        RendezVousResponse resp = service.create(req);
        return ResponseEntity
                .created(URI.create("/api/rendezvous/" + resp.getIdRdv()))
                .body(resp);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<RendezVousResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody RendezVousRequest req) {
        return service.update(id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Valide/soumet un RDV : passe la Demande liée de 'Brouillon' → 'En_attente'.
     * - CLIENT : ne peut soumettre que ses propres demandes.
     * - ADMIN  : peut soumettre n'importe quelle demande.
     */
    @PatchMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<RendezVousResponse> submit(@PathVariable Integer id, Authentication auth) {
        Integer clientIdOrNullIfAdmin = null;

        boolean isClient = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));
        if (isClient) {
            clientIdOrNullIfAdmin = clientResolver.requireCurrentClient(auth).getIdClient();
        }

        return service.submit(id, clientIdOrNullIfAdmin)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
