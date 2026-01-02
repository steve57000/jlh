package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.mapper.DemandeTimelineMapper;
import com.jlh.jlhautopambackend.modeles.*;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import com.jlh.jlhautopambackend.repository.DemandeTimelineRepository;
import com.jlh.jlhautopambackend.repository.StatutDemandeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DemandeTimelineServiceImpl implements DemandeTimelineService {

    private static final String ROLE_SYSTEM = "SYSTEM";
    private static final String ROLE_ADMIN  = "ADMIN";

    private final DemandeRepository demandeRepository;
    private final DemandeTimelineRepository timelineRepository;
    private final StatutDemandeRepository statutRepository;
    private final DemandeTimelineMapper timelineMapper;
    private final UserService userService;

    public DemandeTimelineServiceImpl(DemandeRepository demandeRepository,
                                      DemandeTimelineRepository timelineRepository,
                                      StatutDemandeRepository statutRepository,
                                      DemandeTimelineMapper timelineMapper, UserService userService) {
        this.demandeRepository = demandeRepository;
        this.timelineRepository = timelineRepository;
        this.statutRepository = statutRepository;
        this.timelineMapper = timelineMapper;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<List<DemandeTimelineEntryDto>> listForDemande(Integer demandeId, boolean includeInternal) {
        return demandeRepository.findById(demandeId).map(demande -> {
            List<DemandeTimelineEntryDto> entries = new ArrayList<>();

            if (demande.getTimelineEntries() != null) {
                demande.getTimelineEntries().stream()
                        .sorted(Comparator.comparing(DemandeTimeline::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                        .forEach(entry -> {
                            if (includeInternal || entry.isVisibleClient()) {
                                entries.add(timelineMapper.toDto(entry, userService));
                            }
                        });
            }

            Set<Long> timelineDocumentIds = entries.stream()
                    .map(DemandeTimelineEntryDto::getDocument)
                    .filter(Objects::nonNull)
                    .map(DemandeDocumentDto::getIdDocument)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (demande.getDocuments() != null) {
                demande.getDocuments().forEach(doc -> {
                    if (!includeInternal && !doc.isVisibleClient()) {
                        return;
                    }
                    Long docId = doc.getIdDocument();
                    if (docId != null && timelineDocumentIds.contains(docId)) {
                        return;
                    }
                    entries.add(toDocumentEntry(doc));
                });
            }

            Set<Integer> timelineRdvIds = entries.stream()
                    .map(DemandeTimelineEntryDto::getRendezVous)
                    .filter(Objects::nonNull)
                    .map(RendezVousTimelineDto::getIdRdv)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            RendezVous rendezVous = demande.getRendezVous();
            if (rendezVous != null) {
                Integer rdvId = rendezVous.getIdRdv();
                if (rdvId == null || !timelineRdvIds.contains(rdvId)) {
                    DemandeTimelineEntryDto dto = toRendezVousEntry(rendezVous);
                    if (includeInternal || dto.isVisibleClient()) {
                        entries.add(dto);
                    }
                }
            }

            entries.sort(Comparator.comparing(DemandeTimelineEntryDto::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())));
            return entries;
        });
    }

    @Override
    public DemandeTimelineEntryDto logAdminEvent(Integer demandeId,
                                                 DemandeTimelineRequest request,
                                                 String actorEmail) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new NoSuchElementException("Demande introuvable"));

        DemandeTimelineType type = request.getType();
        DemandeTimeline saved;

        switch (type) {
            case STATUT -> saved = handleAdminStatusChange(demande, request, actorEmail);

            case COMMENTAIRE -> saved = saveCommentEvent(
                    demande,
                    request.getCommentaire(),
                    actorEmail,
                    ROLE_ADMIN,
                    request.getVisibleClient()
            );

            case MONTANT -> {
                BigDecimal montant = request.getMontantValide();
                if (montant == null) {
                    throw new IllegalArgumentException("Le montant est requis.");
                }
                saved = saveMontantEvent(
                        demande,
                        montant,
                        request.getCommentaire(),
                        actorEmail,
                        ROLE_ADMIN,
                        request.getVisibleClient()
                );
            }

            case DOCUMENT -> {
                // create a document-type timeline entry that references the document
                saved = saveDocumentEvent(
                        demande,
                        request.getDocumentId(),
                        request.getDocumentNom(),
                        request.getCommentaire(),
                        actorEmail,
                        ROLE_ADMIN,
                        request.getVisibleClient()
                );
            }

            default -> throw new IllegalArgumentException("Type d'événement non supporté: " + type);
        }

        return timelineMapper.toDto(saved, userService);
    }

    @Override
    public void logStatusChange(Demande demande, StatutDemande newStatut, String previousCode, String actorEmail, String actorRole) {
        saveStatusEvent(demande, newStatut, previousCode, actorEmail, actorRole, null, null, false);
    }

    @Override
    public void logMontantValidation(Demande demande, BigDecimal montant, String commentaire, String actorEmail, String actorRole) {
        saveMontantEvent(demande, montant, commentaire, actorEmail, actorRole, null);
    }

    @Override
    public void logRendezVousEvent(Demande demande, RendezVous rendezVous, String commentaire, String actorEmail, String actorRole) {
        saveRendezVousEvent(demande, rendezVous, commentaire, actorEmail, actorRole, null);
    }

    private DemandeTimeline handleAdminStatusChange(Demande demande, DemandeTimelineRequest request, String actorEmail) {
        String code = request.getCodeStatut();
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Le code statut est requis.");
        }
        StatutDemande statut = statutRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("Statut introuvable: " + code));
        String previous = demande.getStatutDemande() != null ? demande.getStatutDemande().getCodeStatut() : null;
        demande.setStatutDemande(statut);
        Demande updated = demandeRepository.save(demande);
        DemandeTimeline timeline = saveStatusEvent(updated, statut, previous, actorEmail, ROLE_ADMIN,
                request.getCommentaire(), request.getVisibleClient(), true);
        if (timeline == null) {
            throw new IllegalStateException("Aucun changement de statut enregistré.");
        }
        return timeline;
    }

    private DemandeTimeline saveStatusEvent(Demande demande,
                                            StatutDemande statut,
                                            String previousCode,
                                            String actorEmail,
                                            String actorRole,
                                            String commentaire,
                                            Boolean visibleOverride,
                                            boolean force) {
        if (demande == null || statut == null) {
            return null;
        }
        String newCode = statut.getCodeStatut();
        if (!force && previousCode != null && previousCode.equals(newCode)) {
            return null;
        }
        DemandeTimeline timeline = initEvent(demande, DemandeTimelineType.STATUT, actorEmail, actorRole,
                resolveVisible(visibleOverride, true));
        timeline.setStatutCode(newCode);
        timeline.setStatutLibelle(statut.getLibelle());
        timeline.setCommentaire(commentaire);
        DemandeTimeline saved = timelineRepository.save(timeline);
        if (demande.getTimelineEntries() != null) {
            demande.getTimelineEntries().add(saved);
        }
        return saved;
    }

    private DemandeTimeline saveCommentEvent(Demande demande,
                                             String commentaire,
                                             String actorPrenom,
                                             String actorEmail,
                                             Boolean visibleOverride) {
        if (demande == null) {
            return null;
        }
        if (commentaire == null || commentaire.isBlank()) {
            throw new IllegalArgumentException("Le commentaire ne peut pas être vide.");
        }
        DemandeTimeline timeline = initEvent(demande, DemandeTimelineType.COMMENTAIRE, actorPrenom, actorEmail,
                resolveVisible(visibleOverride, false));
        timeline.setCommentaire(commentaire.trim());
        DemandeTimeline saved = timelineRepository.save(timeline);
        if (demande.getTimelineEntries() != null) {
            demande.getTimelineEntries().add(saved);
        }
        return saved;
    }

    private DemandeTimeline saveMontantEvent(Demande demande,
                                             BigDecimal montant,
                                             String commentaire,
                                             String actorEmail,
                                             String actorRole,
                                             Boolean visibleOverride) {
        if (demande == null || montant == null) {
            return null;
        }
        DemandeTimeline timeline = initEvent(demande, DemandeTimelineType.MONTANT, actorEmail, actorRole,
                resolveVisible(visibleOverride, true));
        timeline.setMontantValide(montant);
        timeline.setCommentaire(commentaire);
        DemandeTimeline saved = timelineRepository.save(timeline);
        if (demande.getTimelineEntries() != null) {
            demande.getTimelineEntries().add(saved);
        }
        return saved;
    }

    private DemandeTimeline saveRendezVousEvent(Demande demande,
                                                RendezVous rendezVous,
                                                String commentaire,
                                                String actorEmail,
                                                String actorRole,
                                                Boolean visibleOverride) {
        if (demande == null || rendezVous == null) {
            return null;
        }
        DemandeTimeline timeline = initEvent(demande, DemandeTimelineType.RENDEZVOUS, actorEmail, actorRole,
                resolveVisible(visibleOverride, true));
        timeline.setCommentaire(commentaire);
        timeline.setRendezVousId(rendezVous.getIdRdv());
        StatutRendezVous statut = rendezVous.getStatut();
        if (statut != null) {
            timeline.setRendezVousStatutCode(statut.getCodeStatut());
            timeline.setRendezVousStatutLibelle(statut.getLibelle());
        }
        Creneau creneau = rendezVous.getCreneau();
        if (creneau != null) {
            timeline.setRendezVousDateDebut(creneau.getDateDebut());
            timeline.setRendezVousDateFin(creneau.getDateFin());
        }
        DemandeTimeline saved = timelineRepository.save(timeline);
        if (demande.getTimelineEntries() != null) {
            demande.getTimelineEntries().add(saved);
        }
        return saved;
    }

    private DemandeTimeline initEvent(Demande demande,
                                      DemandeTimelineType type,
                                      String actorEmail,
                                      String actorRole,
                                      boolean visibleClient) {
        return DemandeTimeline.builder()
                .demande(demande)
                .type(type)
                .createdAt(Instant.now())
                .createdBy(actorEmail)
                .createdByRole(resolveRole(actorRole))
                .visibleClient(visibleClient)
                .build();
    }

    private String resolveRole(String actorRole) {
        if (actorRole == null || actorRole.isBlank()) {
            return ROLE_SYSTEM;
        }
        return actorRole;
    }

    private boolean resolveVisible(Boolean override, boolean defaultValue) {
        return override != null ? override : defaultValue;
    }

    private DemandeTimelineEntryDto toDocumentEntry(DemandeDocument document) {
        Instant createdAt = document.getCreeLe() != null ? document.getCreeLe() : Instant.now();
        return DemandeTimelineEntryDto.builder()
                .id(document.getIdDocument())
                .type(DemandeTimelineType.DOCUMENT)
                .createdAt(createdAt)
                .createdBy(document.getCreePar())
                .createdByRole(document.getCreeParRole())
                .visibleClient(document.isVisibleClient())
                .document(DemandeDocumentDto.builder()
                        .idDocument(document.getIdDocument())
                        .nomFichier(document.getNomFichier())
                        .typeContenu(document.getTypeContenu())
                        .tailleOctets(document.getTailleOctets())
                        .visibleClient(document.isVisibleClient())
                        .creePar(document.getCreePar())
                        .creeParRole(document.getCreeParRole())
                        .creeLe(document.getCreeLe())
                        .build())
                .commentaire(document.getNomFichier())
                .source("DOCUMENT")
                .build();
    }

    private DemandeTimelineEntryDto toRendezVousEntry(RendezVous rendezVous) {
        Creneau creneau = rendezVous.getCreneau();
        Instant createdAt = creneau != null ? creneau.getDateDebut() : Instant.now();
        StatutRendezVous statut = rendezVous.getStatut();
        Administrateur admin = rendezVous.getAdministrateur();
        return DemandeTimelineEntryDto.builder()
                .id(rendezVous.getIdRdv() != null ? rendezVous.getIdRdv().longValue() : null)
                .type(DemandeTimelineType.RENDEZVOUS)
                .createdAt(createdAt)
                .createdBy(admin != null ? admin.getEmail() : null)
                .createdByRole(admin != null ? ROLE_ADMIN : ROLE_SYSTEM)
                .visibleClient(true)
                .rendezVous(RendezVousTimelineDto.builder()
                        .idRdv(rendezVous.getIdRdv())
                        .codeStatut(statut != null ? statut.getCodeStatut() : null)
                        .libelleStatut(statut != null ? statut.getLibelle() : null)
                        .dateDebut(creneau != null ? creneau.getDateDebut() : null)
                        .dateFin(creneau != null ? creneau.getDateFin() : null)
                        .build())
                .commentaire(statut != null ? statut.getLibelle() : "Rendez-vous planifié")
                .source("RENDEZVOUS")
                .build();
    }

    private DemandeTimeline saveDocumentEvent(Demande demande,
                                              Long documentId,
                                              String documentNom,
                                              String commentaire,
                                              String actorEmail,
                                              String actorRole,
                                              Boolean visibleOverride) {
        if (demande == null) return null;
        if (documentId == null) {
            throw new IllegalArgumentException("documentId requis pour un événement DOCUMENT.");
        }

        DemandeTimeline timeline = initEvent(demande, DemandeTimelineType.DOCUMENT, actorEmail, actorRole,
                resolveVisible(visibleOverride, true));

        // renseigne les champs spécifiques DOCUMENT (assure-toi que DemandeTimeline a ces colonnes)
        timeline.setDocumentId(documentId);
        timeline.setDocumentNom(documentNom != null ? documentNom : ("Document " + documentId));
        timeline.setCommentaire(commentaire != null ? commentaire : ("Document ajouté : " + documentNom));

        DemandeTimeline saved = timelineRepository.save(timeline);

        if (demande.getTimelineEntries() != null) {
            demande.getTimelineEntries().add(saved);
        }
        return saved;
    }

}
