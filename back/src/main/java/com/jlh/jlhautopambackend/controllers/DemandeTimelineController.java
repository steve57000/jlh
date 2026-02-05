package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.DemandeTimelineEntryDto;
import com.jlh.jlhautopambackend.dto.DemandeTimelineRequest;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.Devis;
import com.jlh.jlhautopambackend.services.DemandeTimelineService;
import com.jlh.jlhautopambackend.services.support.AuthenticatedClientResolver;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import com.jlh.jlhautopambackend.repository.DevisRepository;
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
    private final DevisRepository devisRepository;

    public DemandeTimelineController(DemandeTimelineService timelineService,
                                     DemandeRepository demandeRepository,
                                     AuthenticatedClientResolver clientResolver,
                                     DevisRepository devisRepository) {
        this.timelineService = timelineService;
        this.demandeRepository = demandeRepository;
        this.clientResolver = clientResolver;
        this.devisRepository = devisRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN','MANAGER')")
    public ResponseEntity<List<DemandeTimelineEntryDto>> list(@PathVariable Integer demandeId,
                                                              Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_MANAGER".equals(a.getAuthority()));
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
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<DemandeTimelineEntryDto> create(@PathVariable Integer demandeId,
                                                          @Valid @RequestBody DemandeTimelineRequest request,
                                                          Authentication auth) {
        String actorEmail = auth != null ? auth.getName() : null;
        DemandeTimelineEntryDto dto = timelineService.logAdminEvent(demandeId, request, actorEmail);
        return ResponseEntity.status(201).body(dto);
    }

    @PostMapping("/validation-prix")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> validatePrice(@PathVariable Integer demandeId,
                                              @Valid @RequestBody DemandeTimelineRequest request,
                                              Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) {
            return ResponseEntity.status(401).build();
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_MANAGER".equals(a.getAuthority()));
        String actorRole = auth.getAuthorities().stream().anyMatch(a -> "ROLE_MANAGER".equals(a.getAuthority()))
                ? "MANAGER"
                : "ADMIN";
        String actorEmail = auth != null ? auth.getName() : null;
        if (request.getMontantValide() == null || request.getType() != com.jlh.jlhautopambackend.modeles.DemandeTimelineType.MONTANT) {
            return ResponseEntity.badRequest().build();
        }
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Demande introuvable"));
        timelineService.logMontantValidation(
                demande,
                request.getMontantValide(),
                request.getCommentaire(),
                actorEmail,
                actorRole
        );
        if (isAdmin && demande.getTypeDemande() != null
                && "Devis".equals(demande.getTypeDemande().getCodeType())) {
            Devis devis = devisRepository.findByDemande_IdDemande(demandeId)
                    .orElseGet(() -> {
                        Devis created = new Devis();
                        created.setDemande(demande);
                        created.setDateDevis(java.time.Instant.now());
                        return created;
                    });
            devis.setMontantTotal(request.getMontantValide());
            if (devis.getDateDevis() == null) {
                devis.setDateDevis(java.time.Instant.now());
            }
            devisRepository.save(devis);
        }
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/commentaire")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<DemandeTimelineEntryDto> addClientComment(@PathVariable Integer demandeId,
                                                                    @Valid @RequestBody DemandeTimelineRequest request,
                                                                    Authentication auth) {
        Client client = clientResolver.requireCurrentClient(auth);
        boolean owns = demandeRepository.existsByIdDemandeAndClient_IdClient(demandeId, client.getIdClient());
        if (!owns) {
            return ResponseEntity.status(403).build();
        }
        if (request.getCommentaire() == null || request.getCommentaire().isBlank()) {
            return ResponseEntity.badRequest().body(
                    DemandeTimelineEntryDto.builder()
                            .commentaire("Le commentaire ne peut pas être vide.")
                            .build()
            );
        }
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Demande introuvable"));
        DemandeTimelineEntryDto dto = timelineService.logClientComment(demande, request.getCommentaire(), client.getEmail());
        return ResponseEntity.status(201).body(dto);
    }
}
