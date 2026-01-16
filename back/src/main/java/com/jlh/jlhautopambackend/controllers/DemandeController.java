package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.ClientStatsDto;
import com.jlh.jlhautopambackend.dto.DemandeClientUpdateRequest;
import com.jlh.jlhautopambackend.dto.DemandeRequest;
import com.jlh.jlhautopambackend.dto.DemandeResponse;
import com.jlh.jlhautopambackend.dto.DemandeTimelineEntryDto;
import com.jlh.jlhautopambackend.dto.ProchainRdvDto;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.services.DemandeWorkflowService;
import com.jlh.jlhautopambackend.services.support.AuthenticatedClientResolver;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demandes")
public class DemandeController {

    private final DemandeWorkflowService service;
    private final AuthenticatedClientResolver clientResolver;

    public DemandeController(DemandeWorkflowService service,
                             AuthenticatedClientResolver clientResolver) {
        this.service = service;
        this.clientResolver = clientResolver;
    }

    private Client requireClient(Authentication auth) {
        return clientResolver.requireCurrentClient(auth);
    }

    private DemandeResponse filterTimelineForClient(DemandeResponse response) {
        if (response == null) {
            return null;
        }
        if (response.getTimeline() != null) {
            response.setTimeline(response.getTimeline().stream()
                    .filter(DemandeTimelineEntryDto::isVisibleClient)
                    .toList());
        }
        return response;
    }

    private void assertDemandeEditableForClient(DemandeResponse demande) {
        if (demande == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande introuvable.");
        }
        String statut = demande.getStatutDemande() != null ? demande.getStatutDemande().getCodeStatut() : null;
        Instant now = Instant.now();
        Instant rdvDebut = demande.getRendezVous() != null ? demande.getRendezVous().getDateDebut() : null;
        boolean rdvPasse = rdvDebut != null && !rdvDebut.isAfter(now);
        boolean verrouillee = "Annulee".equals(statut)
                || ("Traitee".equals(statut) && (rdvDebut == null || rdvPasse))
                || rdvPasse;

        if (verrouillee) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La demande est verrouillée. Seul l'ajout de documents reste possible.");
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<DemandeResponse> getAll() { return service.findAll(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<DemandeResponse> getById(@PathVariable Integer id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> createForClient(Authentication auth, @Valid @RequestBody DemandeRequest req) {
        Client client = requireClient(auth);
        try {
            DemandeResponse created = service.createForClient(client.getIdClient(), req);
            URI uri = URI.create("/api/demandes/" + created.getIdDemande());
            return ResponseEntity.created(uri).body(created);
        } catch (IllegalStateException ex) {
            Integer idExistante = service.findCurrentIdForClient(client.getIdClient()).orElse(null);
            assert idExistante != null;
            return ResponseEntity.status(409).body(Map.of(
                    "message", ex.getMessage() != null ? ex.getMessage() : "Une demande en cours existe déjà.",
                    "idDemande", idExistante
            ));
        }
    }

    @PostMapping("/current")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<DemandeResponse> getOrCreate(Authentication auth) {
        Client client = requireClient(auth);

        return service.findCurrentForClient(client.getIdClient())
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    DemandeResponse created = service.getOrCreateForClient(client.getIdClient());
                    return ResponseEntity.created(URI.create("/api/demandes/" + created.getIdDemande())).body(created);
                });
    }

    @GetMapping("/mes-demandes")
    @PreAuthorize("hasRole('CLIENT')")
    public List<DemandeResponse> listForClient(Authentication auth) {
        Client client = requireClient(auth);
        return service.findByClientId(client.getIdClient());
    }

    @GetMapping("/mes-demandes/stats")
    @PreAuthorize("hasRole('CLIENT')")
    public ClientStatsDto statsForClient(Authentication auth) {
        Client client = requireClient(auth);
        return service.findStatsByClientId(client.getIdClient());
    }

    @GetMapping("/mes-demandes/prochain-rdv")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ProchainRdvDto> prochainRdv(Authentication auth) {
        Client client = requireClient(auth);
        return service.findProchainRdvByClientId(client.getIdClient())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<DemandeResponse> update(@PathVariable Integer id, @Valid @RequestBody DemandeRequest req) {
        return service.update(id, req).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // Export ICS (inchangé)
    @GetMapping(value = "/mes-demandes/prochain-rdv.ics", produces = "text/calendar; charset=UTF-8")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<byte[]> prochainRdvIcs(Authentication auth) { /* ... identique à ta version ... */
        Client client = requireClient(auth);
        return service.buildProchainRendezVousIcs(client.getIdClient())
                .map(ics -> {
                    String filename = "rendezvous-" + java.time.format.DateTimeFormatter
                            .ofPattern("yyyyMMdd-HHmm").withZone(java.time.ZoneOffset.UTC)
                            .format(java.time.Instant.now()) + ".ics";
                    return ResponseEntity.ok()
                            .header("Content-Type", "text/calendar; charset=UTF-8")
                            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                            .body(ics.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                })
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping(value = "/rendezvous/{id}/ics", produces = "text/calendar; charset=UTF-8")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN','MANAGER')")
    public ResponseEntity<byte[]> rdvIcs(@PathVariable Integer id, Authentication auth) { /* ... identique ... */
        Integer clientId = null;
        boolean isClient = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));
        if (isClient) {
            clientId = requireClient(auth).getIdClient();
        }
        return service.buildRendezVousIcs(id, clientId)
                .map(ics -> ResponseEntity.ok()
                        .header("Content-Type", "text/calendar; charset=UTF-8")
                        .header("Content-Disposition", "attachment; filename=\"rendezvous-" + id + ".ics\"")
                        .body(ics.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ★ NOUVEAU : submit = passer Brouillon → En_attente (client-owner)
    @PatchMapping("/{id}/submit")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<DemandeResponse> submit(Authentication auth, @PathVariable Integer id) {
        Client client = requireClient(auth);

        // Vérifie ownership (le service.update fera la maj proprement)
        var opt = service.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var d = opt.get();
        if (d.getClient() == null || !client.getIdClient().equals(d.getClient().getIdClient())) {
            return ResponseEntity.status(403).build();
        }
        assertDemandeEditableForClient(d);

        var req = new DemandeRequest();
        req.setCodeStatut("En_attente");
        return service.update(id, req)
                .map(resp -> ResponseEntity.ok(filterTimelineForClient(resp)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/type")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN','MANAGER')")
    public ResponseEntity<DemandeResponse> changeType(
            @PathVariable Integer id,
            @RequestBody Map<String,String> body,
            Authentication auth
    ) {
        String codeType = body.get("codeType");
        if (codeType == null || codeType.isBlank()) return ResponseEntity.badRequest().build();
        boolean isClient = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));
        if (isClient) {
            var opt = service.findById(id);
            if (opt.isEmpty()) return ResponseEntity.notFound().build();
            var d = opt.get();
            Client client = requireClient(auth);
            if (d.getClient() == null || !client.getIdClient().equals(d.getClient().getIdClient())) {
                return ResponseEntity.status(403).build();
            }
            assertDemandeEditableForClient(d);
        }
        return service.update(id, DemandeRequest.builder().codeType(codeType).build())
                .map(resp -> isClient ? filterTimelineForClient(resp) : resp)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/immatriculation")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<DemandeResponse> changeImmatriculation(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body,
            Authentication auth
    ) {
        String immatriculation = body.get("immatriculation");
        Client client = requireClient(auth);
        var opt = service.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var d = opt.get();
        if (d.getClient() == null || !client.getIdClient().equals(d.getClient().getIdClient())) {
            return ResponseEntity.status(403).build();
        }
        assertDemandeEditableForClient(d);

        var req = new DemandeRequest();
        req.setImmatriculation(immatriculation);
        return service.update(id, req)
                .map(resp -> ResponseEntity.ok(filterTimelineForClient(resp)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<DemandeResponse> updateClientInfo(
            @PathVariable Integer id,
            @RequestBody DemandeClientUpdateRequest body,
            Authentication auth
    ) {
        Client client = requireClient(auth);
        var opt = service.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var d = opt.get();
        if (d.getClient() == null || !client.getIdClient().equals(d.getClient().getIdClient())) {
            return ResponseEntity.status(403).build();
        }
        assertDemandeEditableForClient(d);

        var req = new DemandeRequest();
        req.setImmatriculation(body.getImmatriculation());
        req.setVehiculeMarque(body.getVehiculeMarque());
        req.setVehiculeModele(body.getVehiculeModele());
        req.setVehiculeEnergie(body.getVehiculeEnergie());
        req.setTelephone(body.getTelephone());
        req.setAdresseLigne1(body.getAdresseLigne1());
        req.setAdresseLigne2(body.getAdresseLigne2());
        req.setAdresseCodePostal(body.getAdresseCodePostal());
        req.setAdresseVille(body.getAdresseVille());
        return service.update(id, req)
                .map(resp -> ResponseEntity.ok(filterTimelineForClient(resp)))
                .orElse(ResponseEntity.notFound().build());
    }

}
