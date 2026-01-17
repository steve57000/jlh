package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.RendezVousPropositionBatchRequest;
import com.jlh.jlhautopambackend.dto.RendezVousPropositionResponse;
import com.jlh.jlhautopambackend.dto.RendezVousPropositionSlotRequest;
import com.jlh.jlhautopambackend.dto.RendezVousRequest;
import com.jlh.jlhautopambackend.dto.RendezVousResponse;
import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.DemandeService;
import com.jlh.jlhautopambackend.modeles.Devis;
import com.jlh.jlhautopambackend.modeles.RendezVousProposition;
import com.jlh.jlhautopambackend.repository.AdministrateurRepository;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import com.jlh.jlhautopambackend.repository.DemandeServiceRepository;
import com.jlh.jlhautopambackend.repository.RendezVousPropositionRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RendezVousPropositionServiceImpl implements RendezVousPropositionService {

    private static final int MAX_PROPOSITIONS = 3;
    private static final Duration VALIDITY = Duration.ofHours(24);
    private static final String STATUT_PROPOSE = "PROPOSE";
    private static final String STATUT_ACCEPTE = "ACCEPTE";
    private static final String STATUT_REFUSE = "REFUSE";
    private static final String STATUT_EXPIRE = "EXPIRE";
    private static final String STATUT_RDV_CONFIRME = "Confirme";

    private final DemandeRepository demandeRepository;
    private final DemandeServiceRepository demandeServiceRepository;
    private final RendezVousPropositionRepository propositionRepository;
    private final AdministrateurRepository administrateurRepository;
    private final RendezVousService rendezVousService;

    public RendezVousPropositionServiceImpl(
            DemandeRepository demandeRepository,
            DemandeServiceRepository demandeServiceRepository,
            RendezVousPropositionRepository propositionRepository,
            AdministrateurRepository administrateurRepository,
            RendezVousService rendezVousService
    ) {
        this.demandeRepository = demandeRepository;
        this.demandeServiceRepository = demandeServiceRepository;
        this.propositionRepository = propositionRepository;
        this.administrateurRepository = administrateurRepository;
        this.rendezVousService = rendezVousService;
    }

    @Override
    public List<RendezVousPropositionResponse> listByDemande(Integer demandeId, Integer clientIdOrNull) {
        Demande demande = loadDemande(demandeId, clientIdOrNull);
        List<RendezVousProposition> propositions = propositionRepository
                .findByDemande_IdDemandeOrderByDateDebutAsc(demande.getIdDemande());
        Instant now = Instant.now();
        List<RendezVousProposition> expired = propositions.stream()
                .filter(p -> STATUT_PROPOSE.equals(p.getStatut()) && p.getExpiresAt().isBefore(now))
                .peek(p -> p.setStatut(STATUT_EXPIRE))
                .toList();
        if (!expired.isEmpty()) {
            propositionRepository.saveAll(expired);
        }
        return propositions.stream()
                .sorted(Comparator.comparing(RendezVousProposition::getDateDebut))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<RendezVousPropositionResponse> createForDemande(
            Integer demandeId,
            RendezVousPropositionBatchRequest request,
            Integer adminId
    ) {
        Demande demande = loadDemande(demandeId, null);
        Administrateur administrateur = resolveAdmin(adminId);
        List<RendezVousPropositionSlotRequest> slots = request.getPropositions();
        if (slots == null || slots.isEmpty()) {
            throw new IllegalArgumentException("Au moins un créneau est requis.");
        }
        if (slots.size() > MAX_PROPOSITIONS) {
            throw new IllegalArgumentException("Maximum " + MAX_PROPOSITIONS + " créneaux autorisés.");
        }

        Instant now = Instant.now();
        List<RendezVousProposition> existing = propositionRepository
                .findByDemande_IdDemandeAndStatut(demandeId, STATUT_PROPOSE);
        existing.forEach(p -> p.setStatut(STATUT_EXPIRE));

        List<RendezVousProposition> created = slots.stream()
                .filter(Objects::nonNull)
                .map(slot -> createProposition(demande, administrateur, slot, now))
                .collect(Collectors.toList());

        return propositionRepository.saveAll(created).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public RendezVousPropositionResponse accept(Integer demandeId, Integer propositionId, Integer clientIdOrNull, Integer adminIdOrNull) {
        Demande demande = loadDemande(demandeId, clientIdOrNull);
        RendezVousProposition proposition = loadProposition(demandeId, propositionId);
        if (!STATUT_PROPOSE.equals(proposition.getStatut())) {
            throw new IllegalStateException("Ce créneau ne peut plus être validé.");
        }
        Instant now = Instant.now();
        if (proposition.getExpiresAt().isBefore(now)) {
            proposition.setStatut(STATUT_EXPIRE);
            throw new IllegalStateException("Le créneau a expiré.");
        }

        proposition.setStatut(STATUT_ACCEPTE);
        List<RendezVousProposition> others = propositionRepository
                .findByDemande_IdDemandeAndStatut(demandeId, STATUT_PROPOSE);
        others.stream()
                .filter(p -> !p.getIdProposition().equals(propositionId))
                .forEach(p -> p.setStatut(STATUT_REFUSE));

        RendezVousResponse rdv = createRendezVousFromProposition(demande, proposition, adminIdOrNull);
        return toResponse(proposition);
    }

    @Override
    public RendezVousPropositionResponse decline(Integer demandeId, Integer propositionId, Integer clientIdOrNull, Integer adminIdOrNull) {
        loadDemande(demandeId, clientIdOrNull);
        RendezVousProposition proposition = loadProposition(demandeId, propositionId);
        if (!STATUT_PROPOSE.equals(proposition.getStatut())) {
            throw new IllegalStateException("Ce créneau ne peut plus être refusé.");
        }
        Instant now = Instant.now();
        if (proposition.getExpiresAt().isBefore(now)) {
            proposition.setStatut(STATUT_EXPIRE);
        } else {
            proposition.setStatut(STATUT_REFUSE);
        }
        return toResponse(proposition);
    }

    private RendezVousProposition createProposition(
            Demande demande,
            Administrateur administrateur,
            RendezVousPropositionSlotRequest slot,
            Instant now
    ) {
        if (slot.getDateDebut() == null || slot.getDateFin() == null) {
            throw new IllegalArgumentException("Dates de créneau manquantes.");
        }
        if (!slot.getDateFin().isAfter(slot.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin doit être postérieure à la date de début.");
        }
        if (slot.getDateDebut().isBefore(now)) {
            throw new IllegalArgumentException("Le créneau doit être dans le futur.");
        }
        return RendezVousProposition.builder()
                .demande(demande)
                .administrateur(administrateur)
                .dateDebut(slot.getDateDebut())
                .dateFin(slot.getDateFin())
                .statut(STATUT_PROPOSE)
                .createdAt(now)
                .expiresAt(now.plus(VALIDITY))
                .build();
    }

    private RendezVousResponse createRendezVousFromProposition(
            Demande demande,
            RendezVousProposition proposition,
            Integer adminIdOrNull
    ) {
        Integer adminId = adminIdOrNull != null
                ? adminIdOrNull
                : (proposition.getAdministrateur() != null ? proposition.getAdministrateur().getIdAdmin() : null);
        if (adminId == null) {
            throw new IllegalStateException("Administrateur requis pour confirmer un rendez-vous.");
        }

        RendezVousRequest payload = new RendezVousRequest();
        payload.setDemandeId(demande.getIdDemande());
        payload.setDateDebut(proposition.getDateDebut());
        payload.setDateFin(proposition.getDateFin());
        payload.setCodeStatut(STATUT_RDV_CONFIRME);
        payload.setAdministrateurId(adminId);
        payload.setCommentaire("Créneau accepté.");

        Devis devis = demande.getDevis();
        if (devis != null && devis.getIdDevis() != null) {
            return rendezVousService.createForDevis(devis.getIdDevis(), payload, null);
        }

        List<DemandeService> services = demande.getServices() != null && !demande.getServices().isEmpty()
                ? demande.getServices().stream().toList()
                : demandeServiceRepository.findByDemande_IdDemande(demande.getIdDemande());
        if (services != null && !services.isEmpty()) {
            DemandeService firstService = services.stream()
                    .filter(service -> service.getService() != null && service.getService().getIdService() != null)
                    .findFirst()
                    .orElse(null);
            if (firstService != null) {
                Integer serviceId = firstService.getService().getIdService();
                return rendezVousService.createForService(serviceId, payload, null);
            }
        }

        return rendezVousService.createLibre(payload, null);
    }

    private Demande loadDemande(Integer demandeId, Integer clientIdOrNull) {
        if (clientIdOrNull != null) {
            boolean owns = demandeRepository.existsByIdDemandeAndClient_IdClient(demandeId, clientIdOrNull);
            if (!owns) {
                throw new IllegalArgumentException("Demande introuvable.");
            }
        }
        return demandeRepository.findById(demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable."));
    }

    private RendezVousProposition loadProposition(Integer demandeId, Integer propositionId) {
        return propositionRepository.findByIdPropositionAndDemande_IdDemande(propositionId, demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Créneau introuvable."));
    }

    private Administrateur resolveAdmin(Integer adminId) {
        if (adminId == null) {
            throw new IllegalArgumentException("Administrateur requis.");
        }
        return administrateurRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Administrateur introuvable."));
    }

    private RendezVousPropositionResponse toResponse(RendezVousProposition proposition) {
        if (proposition == null) {
            return null;
        }
        Administrateur admin = proposition.getAdministrateur();
        String adminName = null;
        if (admin != null) {
            adminName = (admin.getPrenom() != null ? admin.getPrenom() + " " : "") +
                    (admin.getNom() != null ? admin.getNom() : "");
        }
        return RendezVousPropositionResponse.builder()
                .idProposition(proposition.getIdProposition())
                .dateDebut(proposition.getDateDebut())
                .dateFin(proposition.getDateFin())
                .statut(proposition.getStatut())
                .createdAt(proposition.getCreatedAt())
                .expiresAt(proposition.getExpiresAt())
                .administrateurId(admin != null ? admin.getIdAdmin() : null)
                .administrateurNom(adminName != null && !adminName.isBlank() ? adminName.trim() : null)
                .build();
    }
}
