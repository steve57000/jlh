package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.AvisServiceModerationRequest;
import com.jlh.jlhautopambackend.dto.AvisServiceRequest;
import com.jlh.jlhautopambackend.dto.AvisServiceResponse;
import com.jlh.jlhautopambackend.dto.AvisServiceUpdateRequest;
import com.jlh.jlhautopambackend.modeles.AvisServiceStatut;
import com.jlh.jlhautopambackend.services.AvisServiceService;
import com.jlh.jlhautopambackend.services.support.AuthenticatedClientResolver;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/avis-services")
public class AvisServiceController {

    private final AvisServiceService service;
    private final AuthenticatedClientResolver clientResolver;

    public AvisServiceController(AvisServiceService service, AuthenticatedClientResolver clientResolver) {
        this.service = service;
        this.clientResolver = clientResolver;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<AvisServiceResponse> create(
            Authentication auth,
            @Valid @RequestBody AvisServiceRequest request) {
        Integer clientId = clientResolver.requireCurrentClient(auth).getIdClient();
        AvisServiceResponse created = service.create(clientId, request);
        return ResponseEntity
                .created(URI.create("/api/avis-services/" + created.getIdAvis()))
                .body(created);
    }

    @GetMapping
    public Page<AvisServiceResponse> list(
            @RequestParam(required = false) Integer serviceId,
            @RequestParam(required = false) Integer demandeId,
            @RequestParam(required = false) Integer clientId,
            @RequestParam(required = false) String statut,
            Pageable pageable) {

        // ✅ Cas 1 : filtre serviceId
        if (serviceId != null) {
            if (statut != null) {
                return service.findByService(serviceId, parseStatut(statut), pageable);
            }
            // si pas de statut : par défaut APPROVED (public)
            return service.findByService(serviceId, AvisServiceStatut.APPROVED, pageable);
        }

        // ✅ Cas 2 : filtre demandeId
        if (demandeId != null) {
            return service.findByDemande(demandeId, pageable);
        }

        // ✅ Cas 3 : filtre clientId
        if (clientId != null) {
            return service.findByClient(clientId, pageable);
        }

        // ✅ Cas 4 : filtre statut global (utile pour modération admin)
        if (statut != null) {
            return service.findByStatut(parseStatut(statut), pageable);
        }

        // ✅ Cas 5 : aucun filtre => tous les avis APPROVED (public)
        return service.findApproved(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN','MANAGER')")
    public AvisServiceResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public AvisServiceResponse updateByClient(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody AvisServiceUpdateRequest request) {
        Integer clientId = clientResolver.requireCurrentClient(auth).getIdClient();
        return service.updateByClient(clientId, id, request);
    }

    @PatchMapping("/{id}/moderation")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public AvisServiceResponse moderate(
            @PathVariable Long id,
            @Valid @RequestBody AvisServiceModerationRequest request) {
        return service.moderate(id, request);
    }

    private AvisServiceStatut parseStatut(String statut) {
        try {
            return AvisServiceStatut.valueOf(statut);
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Statut invalide.");
        }
    }
}
