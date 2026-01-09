package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.RendezVousRequest;
import com.jlh.jlhautopambackend.dto.RendezVousResponse;
import com.jlh.jlhautopambackend.mapper.RendezVousMapper;
import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.modeles.Creneau;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.DemandeService;
import com.jlh.jlhautopambackend.modeles.DemandeTimelineType;
import com.jlh.jlhautopambackend.modeles.Devis;
import com.jlh.jlhautopambackend.modeles.RendezVous;
import com.jlh.jlhautopambackend.modeles.StatutCreneau;
import com.jlh.jlhautopambackend.modeles.StatutDemande;
import com.jlh.jlhautopambackend.modeles.StatutRendezVous;
import com.jlh.jlhautopambackend.repository.AdministrateurRepository;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import com.jlh.jlhautopambackend.repository.CreneauRepository;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import com.jlh.jlhautopambackend.repository.DemandeServiceRepository;
import com.jlh.jlhautopambackend.repository.DemandeTimelineRepository;
import com.jlh.jlhautopambackend.repository.DevisRepository;
import com.jlh.jlhautopambackend.repository.RendezVousRepository;
import com.jlh.jlhautopambackend.repository.StatutCreneauRepository;
import com.jlh.jlhautopambackend.repository.StatutDemandeRepository;
import com.jlh.jlhautopambackend.repository.StatutRendezVousRepository;
import com.jlh.jlhautopambackend.repository.TypeDemandeRepository;
import com.jlh.jlhautopambackend.services.DemandeTimelineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RendezVousServiceImpl implements RendezVousService {

    private final RendezVousRepository repo;
    private final DemandeRepository demandeRepo;
    private final DemandeServiceRepository demandeServiceRepository;
    private final DevisRepository devisRepository;
    private final CreneauRepository creneauRepo;
    private final AdministrateurRepository adminRepo;
    private final StatutCreneauRepository statutCreneauRepo;
    private final StatutRendezVousRepository statutRendezVousRepo;
    private final StatutDemandeRepository statutDemandeRepo;
    private final TypeDemandeRepository typeDemandeRepo;
    private final RendezVousMapper mapper;
    private final DemandeTimelineService timelineService;
    private final DemandeTimelineRepository timelineRepository;

    private static final String STATUT_BROUILLON  = "Brouillon";
    private static final String STATUT_EN_ATTENTE = "En_attente";
    private static final String TYPE_RENDEZ_VOUS  = "RendezVous";
    private static final String STATUT_CRENEAU_RESERVE = "Reserve";

    public RendezVousServiceImpl(RendezVousRepository repo,
                                 DemandeRepository demandeRepo,
                                 DemandeServiceRepository demandeServiceRepository,
                                 DevisRepository devisRepository,
                                 CreneauRepository creneauRepo,
                                 AdministrateurRepository adminRepo,
                                 StatutCreneauRepository statutCreneauRepo,
                                 StatutRendezVousRepository statutRendezVousRepo,
                                 StatutDemandeRepository statutDemandeRepo,
                                 TypeDemandeRepository typeDemandeRepo,
                                 RendezVousMapper mapper,
                                 DemandeTimelineService timelineService,
                                 DemandeTimelineRepository timelineRepository) {
        this.repo = repo;
        this.demandeRepo = demandeRepo;
        this.demandeServiceRepository = demandeServiceRepository;
        this.devisRepository = devisRepository;
        this.creneauRepo = creneauRepo;
        this.adminRepo = adminRepo;
        this.statutCreneauRepo = statutCreneauRepo;
        this.statutRendezVousRepo = statutRendezVousRepo;
        this.statutDemandeRepo = statutDemandeRepo;
        this.typeDemandeRepo = typeDemandeRepo;
        this.mapper = mapper;
        this.timelineService = timelineService;
        this.timelineRepository = timelineRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponse> findAll() {
        return repo.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RendezVousResponse> findById(Integer id) {
        return repo.findById(id).map(mapper::toResponse);
    }

    @Override
    public RendezVousResponse createLibre(RendezVousRequest req, Integer clientId) {
        Demande demande = resolveDemande(req.getDemandeId());
        if (demande == null) {
            throw new IllegalArgumentException("Demande libre requise pour créer un rendez-vous.");
        }
        Creneau creneau = resolveCreneau(req);
        Administrateur admin = resolveAdmin(req.getAdministrateurId());
        StatutRendezVous statutRdv = resolveStatut(req.getCodeStatut());
        validateClient(req.getClientId(), clientId, demande);

        if (demande.getTypeDemande() == null || !TYPE_RENDEZ_VOUS.equals(demande.getTypeDemande().getCodeType())) {
            if (demande.getTypeDemande() != null) {
                throw new IllegalStateException("Le rendez-vous libre ne peut être créé que pour une demande libre.");
            }
            var typeLibre = typeDemandeRepo.findById(TYPE_RENDEZ_VOUS)
                    .orElseThrow(() -> new IllegalStateException("TypeDemande 'RendezVous' manquant"));
            demande.setTypeDemande(typeLibre);
            demandeRepo.save(demande);
        }

        RendezVous ent = mapper.toEntity(req);
        ent.setDemande(demande);
        ent.setCreneau(creneau);
        ent.setAdministrateur(admin);
        ent.setStatut(statutRdv);

        RendezVous saved = repo.save(ent);
        timelineService.logRendezVousEvent(demande, saved, "Rendez-vous planifié", admin.getEmail(), "ADMIN");
        return mapper.toResponse(saved);
    }

    @Override
    public RendezVousResponse createForService(Integer serviceId, RendezVousRequest req, Integer clientId) {
        if (req.getDemandeId() == null) {
            throw new IllegalArgumentException("demandeId requis pour un rendez-vous lié à un service.");
        }
        DemandeService demandeService = demandeServiceRepository
                .findByDemande_IdDemandeAndService_IdService(req.getDemandeId(), serviceId)
                .orElseThrow(() -> new IllegalArgumentException("DemandeService introuvable pour le service " + serviceId));
        Demande demande = demandeService.getDemande();
        assertPriceValidated(demande);

        Creneau creneau = resolveCreneau(req);
        Administrateur admin = resolveAdmin(req.getAdministrateurId());
        StatutRendezVous statutRdv = resolveStatut(req.getCodeStatut());
        validateClient(req.getClientId(), clientId, demande);

        RendezVous ent = mapper.toEntity(req);
        ent.setDemande(demande);
        ent.setDemandeService(demandeService);
        ent.setCreneau(creneau);
        ent.setAdministrateur(admin);
        ent.setStatut(statutRdv);

        RendezVous saved = repo.save(ent);
        timelineService.logRendezVousEvent(demande, saved, "Rendez-vous planifié", admin.getEmail(), "ADMIN");
        return mapper.toResponse(saved);
    }

    @Override
    public RendezVousResponse createForDevis(Integer devisId, RendezVousRequest req, Integer clientId) {
        Devis devis = devisRepository.findById(devisId)
                .orElseThrow(() -> new IllegalArgumentException("Devis introuvable: " + devisId));
        Demande demande = devis.getDemande();
        assertPriceValidated(demande);

        Creneau creneau = resolveCreneau(req);
        Administrateur admin = resolveAdmin(req.getAdministrateurId());
        StatutRendezVous statutRdv = resolveStatut(req.getCodeStatut());
        validateClient(req.getClientId(), clientId, demande);

        RendezVous ent = mapper.toEntity(req);
        ent.setDemande(demande);
        ent.setDevis(devis);
        ent.setCreneau(creneau);
        ent.setAdministrateur(admin);
        ent.setStatut(statutRdv);

        RendezVous saved = repo.save(ent);
        timelineService.logRendezVousEvent(demande, saved, "Rendez-vous planifié", admin.getEmail(), "ADMIN");
        return mapper.toResponse(saved);
    }

    @Override
    public Optional<RendezVousResponse> update(Integer id, RendezVousRequest req) {
        return repo.findById(id).map(existing -> {
            if (req.getDemandeId() != null) {
                existing.setDemande(demandeRepo.findById(req.getDemandeId())
                        .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + req.getDemandeId())));
            }
            if (req.getCreneauId() != null || (req.getDateDebut() != null && req.getDateFin() != null)) {
                existing.setCreneau(resolveCreneau(req));
            }
            if (req.getAdministrateurId() != null) {
                existing.setAdministrateur(resolveAdmin(req.getAdministrateurId()));
            }
            existing.setStatut(resolveStatut(req.getCodeStatut()));
            if (req.getCommentaire() != null) {
                existing.setCommentaire(req.getCommentaire());
            }
            RendezVous updated = repo.save(existing);
            if (updated.getDemande() != null) {
                timelineService.logRendezVousEvent(updated.getDemande(), updated, "Rendez-vous mis à jour",
                        updated.getAdministrateur() != null ? updated.getAdministrateur().getEmail() : null, "ADMIN");
            }
            return mapper.toResponse(updated);
        });
    }

    @Override
    public Optional<RendezVousResponse> submit(Integer rdvId, Integer clientIdOrNullIfAdmin) {
        Optional<RendezVous> opt = (clientIdOrNullIfAdmin == null)
                ? repo.findById(rdvId) // ADMIN
                : repo.findByIdAndClient(rdvId, clientIdOrNullIfAdmin); // CLIENT (ownership)

        if (opt.isEmpty()) return Optional.empty();

        RendezVous rdv = opt.get();
        Demande demande = rdv.getDemande();
        if (demande == null) return Optional.empty();

        String current = demande.getStatutDemande() != null ? demande.getStatutDemande().getCodeStatut() : null;
        if (current == null || STATUT_BROUILLON.equals(current)) {
            StatutDemande enAttente = statutDemandeRepo.findById(STATUT_EN_ATTENTE)
                    .orElseThrow(() -> new IllegalStateException("Statut 'En_attente' manquant en base"));
            demande.setStatutDemande(enAttente);
            Demande updated = demandeRepo.save(demande);
            timelineService.logStatusChange(updated, enAttente, current, null, null);
        }

        return Optional.of(mapper.toResponse(rdv));
    }

    @Override
    public boolean delete(Integer id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }

    private Demande resolveDemande(Integer demandeId) {
        if (demandeId == null) {
            return null;
        }
        return demandeRepo.findById(demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + demandeId));
    }

    private Creneau resolveCreneau(RendezVousRequest request) {
        Integer creneauId = request.getCreneauId();
        if (creneauId != null) {
            return creneauRepo.findById(creneauId)
                    .orElseThrow(() -> new IllegalArgumentException("Creneau introuvable: " + creneauId));
        }
        if (request.getDateDebut() == null || request.getDateFin() == null) {
            throw new IllegalArgumentException("Creneau requis pour le rendez-vous.");
        }
        StatutCreneau statut = statutCreneauRepo.findById(STATUT_CRENEAU_RESERVE)
                .orElseThrow(() -> new IllegalStateException("Statut creneau 'Reserve' manquant"));
        Creneau creneau = Creneau.builder()
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .statut(statut)
                .build();
        return creneauRepo.save(creneau);
    }

    private Administrateur resolveAdmin(Integer adminId) {
        if (adminId == null) {
            throw new IllegalArgumentException("Administrateur requis pour le rendez-vous.");
        }
        return adminRepo.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Administrateur introuvable: " + adminId));
    }

    private StatutRendezVous resolveStatut(String codeStatut) {
        return statutRendezVousRepo.findById(codeStatut)
                .orElseThrow(() -> new IllegalArgumentException("Statut RDV introuvable: " + codeStatut));
    }

    private void validateClient(Integer requestClientId, Integer authClientId, Demande demande) {
        Integer resolvedId = requestClientId != null ? requestClientId : authClientId;
        if (resolvedId == null && demande != null && demande.getClient() != null) {
            return;
        }
        if (resolvedId == null) {
            throw new IllegalArgumentException("Client requis pour le rendez-vous.");
        }
        if (demande != null && demande.getClient() != null
                && !demande.getClient().getIdClient().equals(resolvedId)) {
            throw new IllegalArgumentException("Le client ne correspond pas à la demande.");
        }
    }

    private void assertPriceValidated(Demande demande) {
        if (demande == null) {
            throw new IllegalArgumentException("Demande introuvable pour validation du prix.");
        }
        boolean validated = timelineRepository.existsByDemande_IdDemandeAndType(
                demande.getIdDemande(), DemandeTimelineType.MONTANT);
        if (!validated) {
            throw new IllegalStateException("Validation du prix requise avant création du rendez-vous.");
        }
    }
}
