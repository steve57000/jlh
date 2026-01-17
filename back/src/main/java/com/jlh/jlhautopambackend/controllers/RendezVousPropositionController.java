package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.RendezVousPropositionBatchRequest;
import com.jlh.jlhautopambackend.dto.RendezVousPropositionResponse;
import com.jlh.jlhautopambackend.repository.AdministrateurRepository;
import com.jlh.jlhautopambackend.services.RendezVousPropositionService;
import com.jlh.jlhautopambackend.services.support.AuthenticatedClientResolver;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demandes/{demandeId}/rendezvous-propositions")
public class RendezVousPropositionController {

    private final RendezVousPropositionService service;
    private final AuthenticatedClientResolver clientResolver;
    private final AdministrateurRepository adminRepository;

    public RendezVousPropositionController(
            RendezVousPropositionService service,
            AuthenticatedClientResolver clientResolver,
            AdministrateurRepository adminRepository
    ) {
        this.service = service;
        this.clientResolver = clientResolver;
        this.adminRepository = adminRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN','MANAGER')")
    public ResponseEntity<List<RendezVousPropositionResponse>> list(
            @PathVariable Integer demandeId,
            Authentication auth
    ) {
        Integer clientId = resolveClientId(auth);
        return ResponseEntity.ok(service.listByDemande(demandeId, clientId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<RendezVousPropositionResponse>> create(
            @PathVariable Integer demandeId,
            @Valid @RequestBody RendezVousPropositionBatchRequest request,
            Authentication auth
    ) {
        Integer adminId = resolveAdminId(auth);
        return ResponseEntity.ok(service.createForDemande(demandeId, request, adminId));
    }

    @PatchMapping("/{propositionId}/accept")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN','MANAGER')")
    public ResponseEntity<RendezVousPropositionResponse> accept(
            @PathVariable Integer demandeId,
            @PathVariable Integer propositionId,
            Authentication auth
    ) {
        Integer clientId = resolveClientId(auth);
        Integer adminId = resolveAdminId(auth);
        return ResponseEntity.ok(service.accept(demandeId, propositionId, clientId, adminId));
    }

    @PatchMapping("/{propositionId}/decline")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN','MANAGER')")
    public ResponseEntity<RendezVousPropositionResponse> decline(
            @PathVariable Integer demandeId,
            @PathVariable Integer propositionId,
            Authentication auth
    ) {
        Integer clientId = resolveClientId(auth);
        Integer adminId = resolveAdminId(auth);
        return ResponseEntity.ok(service.decline(demandeId, propositionId, clientId, adminId));
    }

    private Integer resolveClientId(Authentication auth) {
        if (auth == null) return null;
        boolean isClient = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));
        if (!isClient) return null;
        return clientResolver.requireCurrentClient(auth).getIdClient();
    }

    private Integer resolveAdminId(Authentication auth) {
        if (auth == null) return null;
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));
        if (!isAdmin) return null;
        return adminRepository.findByEmail(auth.getName())
                .map(a -> a.getIdAdmin())
                .orElse(null);
    }
}
