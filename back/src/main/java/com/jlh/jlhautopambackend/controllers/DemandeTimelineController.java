package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.DemandeTimelineEntryDto;
import com.jlh.jlhautopambackend.dto.DemandeTimelineRequest;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.services.DemandeTimelineService;
import com.jlh.jlhautopambackend.services.support.AuthenticatedClientResolver;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandes/{demandeId}/timeline")
public class DemandeTimelineController {

    private final DemandeTimelineService timelineService;
    private final DemandeRepository demandeRepository;
    private final AuthenticatedClientResolver clientResolver;

    public DemandeTimelineController(DemandeTimelineService timelineService,
                                     DemandeRepository demandeRepository,
                                     AuthenticatedClientResolver clientResolver) {
        this.timelineService = timelineService;
        this.demandeRepository = demandeRepository;
        this.clientResolver = clientResolver;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<List<DemandeTimelineEntryDto>> list(@PathVariable Integer demandeId,
                                                              Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!isAdmin) {
            Client client = clientResolver.requireCurrentClient(auth);
            boolean owns = demandeRepository.existsByIdDemandeAndClient_IdClient(demandeId, client.getIdClient());
            if (!owns) {
                return ResponseEntity.status(403).build();
            }
        }

        return timelineService.listForDemande(demandeId, isAdmin)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DemandeTimelineEntryDto> create(@PathVariable Integer demandeId,
                                                          @Valid @RequestBody DemandeTimelineRequest request,
                                                          Authentication auth) {
        String actorEmail = auth != null ? auth.getName() : null;
        DemandeTimelineEntryDto dto = timelineService.logAdminEvent(demandeId, request, actorEmail);
        return ResponseEntity.status(201).body(dto);
    }
}
