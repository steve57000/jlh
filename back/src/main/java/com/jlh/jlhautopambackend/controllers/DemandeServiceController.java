package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import com.jlh.jlhautopambackend.services.DemandeServiceService;
import com.jlh.jlhautopambackend.services.support.AuthenticatedClientResolver;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/demandes-services")
public class DemandeServiceController {

    private final DemandeServiceService service;
    private final DemandeRepository demandeRepo;
    private final AuthenticatedClientResolver clientResolver;

    public DemandeServiceController(
            DemandeServiceService service,
            DemandeRepository demandeRepo,
            AuthenticatedClientResolver clientResolver
    ) {
        this.service = service;
        this.demandeRepo = demandeRepo;
        this.clientResolver = clientResolver;
    }

    /* ==================== ADMIN ==================== */

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<DemandeServiceResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{demandeId}/{serviceId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<DemandeServiceResponse> getByKey(
            @PathVariable Integer demandeId,
            @PathVariable Integer serviceId) {
        return service.findByKey(demandeId, serviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /* ==================== CLIENT (ownership) ==================== */

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CLIENT','MANAGER')")
    public ResponseEntity<DemandeServiceResponse> create(
            Authentication auth,
            @Valid @RequestBody DemandeServiceRequest req) {

        if (!isAdmin(auth)) {
            assertEditableDemandeForClient(auth, req.getDemandeId());
        }

        DemandeServiceResponse resp = service.create(req);
        String path = String.format("/api/demandes-services/%d/%d",
                resp.getId().getIdDemande(),
                resp.getId().getIdService());
        return ResponseEntity.created(URI.create(path)).body(resp);
    }

    @PutMapping("/{demandeId}/{serviceId}")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENT','MANAGER')")
    public ResponseEntity<DemandeServiceResponse> update(
            Authentication auth,
            @PathVariable Integer demandeId,
            @PathVariable Integer serviceId,
            @Valid @RequestBody DemandeServiceRequest req) {

        if (!isAdmin(auth)) {
            assertEditableDemandeForClient(auth, demandeId);
        }

        return service.update(demandeId, serviceId, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{demandeId}/{serviceId}")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENT','MANAGER')")
    public ResponseEntity<Void> delete(
            Authentication auth,
            @PathVariable Integer demandeId,
            @PathVariable Integer serviceId) {

        if (!isAdmin(auth)) {
            assertEditableDemandeForClient(auth, demandeId);
        }

        return service.delete(demandeId, serviceId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /* ==================== Utils ==================== */

    private Integer getClientIdFromAuth(Authentication auth) {
        return clientResolver.requireCurrentClient(auth).getIdClient();
    }

    private void assertEditableDemandeForClient(Authentication auth, Integer demandeId) {
        Integer clientId = getClientIdFromAuth(auth);
        var demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande introuvable."));
        if (demande.getClient() == null || !Objects.equals(demande.getClient().getIdClient(), clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action non autorisée.");
        }
        String statut = demande.getStatutDemande() != null ? demande.getStatutDemande().getCodeStatut() : null;
        if (statut != null && !"Brouillon".equals(statut)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La demande n'est plus modifiable tant qu'elle est en attente de traitement.");
        }
    }

    private boolean isAdmin(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(granted -> "ROLE_ADMIN".equals(granted.getAuthority())
                        || "ROLE_MANAGER".equals(granted.getAuthority()));
    }
}
