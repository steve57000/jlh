package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.config.GarageProperties;
import com.jlh.jlhautopambackend.dto.ClientStatsDto;
import com.jlh.jlhautopambackend.dto.DemandeRequest;
import com.jlh.jlhautopambackend.dto.DemandeResponse;
import com.jlh.jlhautopambackend.dto.ProchainRdvDto;
import com.jlh.jlhautopambackend.mapper.DevisMapper;
import com.jlh.jlhautopambackend.mapper.DemandeMapper;
import com.jlh.jlhautopambackend.mapper.RendezVousMapper;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.RendezVous;
import com.jlh.jlhautopambackend.modeles.StatutDemande;
import com.jlh.jlhautopambackend.modeles.TypeDemande;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import com.jlh.jlhautopambackend.repository.DevisRepository;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import com.jlh.jlhautopambackend.repository.DemandeServiceRepository;
import com.jlh.jlhautopambackend.repository.RendezVousRepository;
import com.jlh.jlhautopambackend.repository.StatutDemandeRepository;
import com.jlh.jlhautopambackend.repository.TypeDemandeRepository;
import com.jlh.jlhautopambackend.utils.IcsUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DemandeServiceImpl implements DemandeWorkflowService {
    private static final String TYPE_DEFAULT = "Devis";
    private static final String STATUT_BROUILLON = "Brouillon";
    private static final String STATUT_EN_ATTENTE = "En_attente";
    private static final String STATUT_TRAITEE = "Traitee";
    private static final String STATUT_ANNULEE = "Annulee";
    private static final String TYPE_RENDEZ_VOUS = "RendezVous";
    private static final String TYPE_SERVICE = "Service";
    private static final String TYPE_DEVIS = "Devis";

    private final DemandeRepository repository;
    private final ClientRepository clientRepo;
    private final TypeDemandeRepository typeRepo;
    private final StatutDemandeRepository statutRepo;
    private final RendezVousRepository rendezVousRepository;
    private final DemandeServiceRepository demandeServiceRepository;
    private final DevisRepository devisRepository;
    private final DemandeMapper mapper;
    private final DevisMapper devisMapper;
    private final RendezVousMapper rendezVousMapper;
    private final DemandeTimelineService timelineService;
    private final GarageProperties garageProperties;
    private final UserService userService;

    public DemandeServiceImpl(DemandeRepository repository,
                              ClientRepository clientRepo,
                              TypeDemandeRepository typeRepo,
                              StatutDemandeRepository statutRepo,
                              DemandeMapper mapper,
                              RendezVousRepository rendezVousRepository,
                              DemandeServiceRepository demandeServiceRepository,
                              DevisRepository devisRepository,
                              DevisMapper devisMapper,
                              RendezVousMapper rendezVousMapper,
                              DemandeTimelineService timelineService,
                              GarageProperties garageProperties, UserService userService) {
        this.repository = repository;
        this.clientRepo = clientRepo;
        this.typeRepo = typeRepo;
        this.statutRepo = statutRepo;
        this.mapper = mapper;
        this.timelineService = timelineService;
        this.rendezVousRepository = rendezVousRepository;
        this.demandeServiceRepository = demandeServiceRepository;
        this.devisRepository = devisRepository;
        this.devisMapper = devisMapper;
        this.rendezVousMapper = rendezVousMapper;
        this.garageProperties = garageProperties;
        this.userService = userService;
    }

    @Override
    public ClientStatsDto findStatsByClientId(Integer clientId) {
        long enAttente = repository.countByClient_IdClientAndStatutDemande_CodeStatut(clientId, STATUT_EN_ATTENTE);
        long traitees = repository.countByClient_IdClientAndStatutDemande_CodeStatut(clientId, STATUT_TRAITEE);
        long annulees = repository.countByClient_IdClientAndStatutDemande_CodeStatut(clientId, STATUT_ANNULEE);
        long rdvAvenir = rendezVousRepository.countUpcomingByClientId(clientId, Instant.now());
        long demandesLibres = repository.countByClient_IdClientAndTypeDemande_CodeType(clientId, TYPE_RENDEZ_VOUS);
        long demandesService = repository.countByClient_IdClientAndTypeDemande_CodeType(clientId, TYPE_SERVICE);
        long demandesDevis = repository.countByClient_IdClientAndTypeDemande_CodeType(clientId, TYPE_DEVIS);
        long rdvNonLies = rendezVousRepository.countByDemande_Client_IdClientAndDemandeServiceIsNullAndDevisIsNull(clientId);
        long rdvLies = rendezVousRepository.countLinkedByClientId(clientId);
        return new ClientStatsDto(enAttente, traitees, annulees, rdvAvenir,
                demandesLibres, demandesService, demandesDevis, rdvLies, rdvNonLies);
    }

    @Override
    public Optional<ProchainRdvDto> findProchainRdvByClientId(Integer clientId) {
        return rendezVousRepository.findUpcomingByClientId(clientId, Instant.now())
                .stream()
                .findFirst()
                .map(this::toProchainRdvDto);
    }

    @Override
    public DemandeResponse create(DemandeRequest request) {
        Demande entity = mapper.toEntity(request);
        entity.setDateDemande(resolveDate(request.getDateDemande()));
        Client client = clientRepo.findById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable : " + request.getClientId()));
        TypeDemande type = typeRepo.findById(request.getCodeType())
                .orElseThrow(() -> new IllegalArgumentException("TypeDemande introuvable: " + request.getCodeType()));
        StatutDemande statut = statutRepo.findById(request.getCodeStatut())
                .orElseThrow(() -> new IllegalArgumentException("StatutDemande introuvable: " + request.getCodeStatut()));
        entity.setClient(client);
        entity.setTypeDemande(type);
        entity.setStatutDemande(statut);
        Demande saved = repository.save(entity);
        timelineService.logStatusChange(saved, statut, null, null, "ADMIN");
        return enrichDemandeResponse(saved, mapper.toResponse(saved, userService));
    }

    @Override
    public DemandeResponse createPublic() {
        throw new UnsupportedOperationException("La création publique de demandes n'est pas supportée.");
    }

    @Override
    public DemandeResponse createForClient(Integer clientId, DemandeRequest request) {
        if (clientId == null) {
            throw new IllegalArgumentException("L'identifiant client est requis");
        }
        ensureNoCurrentDemande(clientId);

        DemandeRequest payload = request != null ? request : new DemandeRequest();
        Demande entity = mapper.toEntity(payload);
        entity.setDateDemande(resolveDate(payload.getDateDemande()));

        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable : " + clientId));
        entity.setClient(client);

        String typeCode = (payload.getCodeType() == null || payload.getCodeType().isBlank())
                ? TYPE_DEFAULT
                : payload.getCodeType();
        TypeDemande type = typeRepo.findById(typeCode)
                .orElseThrow(() -> new IllegalArgumentException("TypeDemande introuvable: " + typeCode));
        entity.setTypeDemande(type);

        String statutCode = (payload.getCodeStatut() == null || payload.getCodeStatut().isBlank())
                ? STATUT_BROUILLON
                : payload.getCodeStatut();
        StatutDemande statut = statutRepo.findById(statutCode)
                .orElseThrow(() -> new IllegalArgumentException("StatutDemande introuvable: " + statutCode));
        entity.setStatutDemande(statut);

        Demande saved = repository.save(entity);
        timelineService.logStatusChange(saved, statut, null, client.getEmail(), "CLIENT");
        return enrichDemandeResponse(saved, mapper.toResponse(saved, userService));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DemandeResponse> findById(Integer id) {
        return repository.findById(id)
                .map(demande -> enrichDemandeResponse(demande, mapper.toResponse(demande, userService)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(demande -> enrichDemandeResponse(demande, mapper.toResponse(demande, userService)))
                .collect(Collectors.toList());
    }

    @Override
    public List<DemandeResponse> findByClientId(Integer clientId) {
        return repository.findByClient_IdClientOrderByDateDemandeDesc(clientId)
                .stream()
                .map(demande -> enrichDemandeResponse(demande, mapper.toResponse(demande, userService)))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DemandeResponse> update(Integer id, DemandeRequest request) {
        return repository.findById(id)
                .map(existing -> {
                    if (request.getDateDemande() != null) {
                        existing.setDateDemande(request.getDateDemande());
                    }

                    if (request.getClientId() != null && !request.getClientId().equals(
                            existing.getClient() != null ? existing.getClient().getIdClient() : null)) {
                        Client client = clientRepo.findById(request.getClientId())
                                .orElseThrow(() -> new IllegalArgumentException("Client introuvable : " + request.getClientId()));
                        existing.setClient(client);
                    }

                    if (request.getCodeType() != null && !request.getCodeType().isBlank()) {
                        TypeDemande type = typeRepo.findById(request.getCodeType())
                                .orElseThrow(() -> new IllegalArgumentException("TypeDemande introuvable: " + request.getCodeType()));
                        existing.setTypeDemande(type);
                    }

                    String previousStatut = existing.getStatutDemande() != null
                            ? existing.getStatutDemande().getCodeStatut()
                            : null;
                    boolean statutChanged = false;
                    if (request.getCodeStatut() != null && !request.getCodeStatut().isBlank()) {
                        StatutDemande statut = statutRepo.findById(request.getCodeStatut())
                                .orElseThrow(() -> new IllegalArgumentException("StatutDemande introuvable: " + request.getCodeStatut()));
                        existing.setStatutDemande(statut);
                        statutChanged = previousStatut == null || !previousStatut.equals(statut.getCodeStatut());
                    }

                    if (request.getServices() != null && !request.getServices().isEmpty()) {
                        applyServiceUpdates(existing, request.getServices());
                    }

                    Demande saved = repository.save(existing);
                    if (statutChanged) {
                        timelineService.logStatusChange(saved, saved.getStatutDemande(), previousStatut, null, null);
                    }
                    return enrichDemandeResponse(saved, mapper.toResponse(saved, userService));
                });
    }

    @Override
    public boolean delete(Integer id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    @Override
    public Optional<String> buildProchainRendezVousIcs(Integer clientId) {
        return rendezVousRepository.findUpcomingByClientId(clientId, Instant.now())
                .stream()
                .findFirst()
                .map(this::buildIcs);
    }

    @Override
    public Optional<String> buildRendezVousIcs(Integer rdvId, Integer clientIdOrNullIfAdmin) {
        Optional<RendezVous> rdvOpt = (clientIdOrNullIfAdmin == null)
                ? rendezVousRepository.findById(rdvId)
                : rendezVousRepository.findByIdAndClient(rdvId, clientIdOrNullIfAdmin);
        return rdvOpt.map(this::buildIcs);
    }

    @Override
    public Optional<Integer> findCurrentIdForClient(Integer clientId) {
        return findCurrentForClient(clientId).map(DemandeResponse::getIdDemande);
    }

    @Override
    public Optional<DemandeResponse> findCurrentForClient(Integer clientId) {
        return repository.findFirstByClient_IdClientAndStatutDemande_CodeStatutOrderByDateDemandeDesc(clientId, STATUT_BROUILLON)
                .or(() -> repository.findFirstByClient_IdClientAndStatutDemande_CodeStatutOrderByDateDemandeDesc(clientId, STATUT_EN_ATTENTE))
                .map(demande -> enrichDemandeResponse(demande, mapper.toResponse(demande, userService)));
    }

    @Override
    public DemandeResponse getOrCreateForClient(Integer clientId) {
        return findCurrentForClient(clientId)
                .orElseGet(() -> {
                    DemandeRequest draft = DemandeRequest.builder()
                            .dateDemande(Instant.now())
                            .codeType(TYPE_DEFAULT)
                            .codeStatut(STATUT_BROUILLON)
                            .build();
                    return createForClient(clientId, draft);
                });
    }

    private void ensureNoCurrentDemande(Integer clientId) {
        if (findCurrentForClient(clientId).isPresent()) {
            throw new IllegalStateException("Une demande en cours existe déjà.");
        }
    }

    private Instant resolveDate(Instant provided) {
        return provided != null ? provided : Instant.now();
    }

    private ProchainRdvDto toProchainRdvDto(RendezVous rendezVous) {
        if (rendezVous == null || rendezVous.getCreneau() == null) {
            return null;
        }
        return ProchainRdvDto.builder()
                .idRdv(rendezVous.getIdRdv())
                .codeStatut(rendezVous.getStatut() != null ? rendezVous.getStatut().getCodeStatut() : null)
                .libelleStatut(rendezVous.getStatut() != null ? rendezVous.getStatut().getLibelle() : null)
                .dateDebut(rendezVous.getCreneau().getDateDebut())
                .dateFin(rendezVous.getCreneau().getDateFin())
                .build();
    }

    private String buildIcs(RendezVous rendezVous) {
        if (rendezVous == null || rendezVous.getCreneau() == null) {
            return null;
        }
        Instant start = rendezVous.getCreneau().getDateDebut();
        Instant end = rendezVous.getCreneau().getDateFin();
        String summary = "Rendez-vous " + defaultGarageName();
        String description = buildDescription(rendezVous);
        String location = defaultGarageAddress();
        String organizerEmail = garageProperties != null ? garageProperties.getOrganizerEmail() : null;
        String uidSeed = "rdv-" + rendezVous.getIdRdv() + '-' + (start != null ? start.toString() : Instant.now().toString());
        return IcsUtil.veventWithAlarms(
                IcsUtil.uid(uidSeed),
                summary,
                description,
                location,
                start,
                end,
                defaultGarageName(),
                organizerEmail
        );
    }

    private String buildDescription(RendezVous rendezVous) {
        StringBuilder sb = new StringBuilder();
        if (rendezVous.getDemande() != null) {
            sb.append("Demande #").append(rendezVous.getDemande().getIdDemande());
            Client client = rendezVous.getDemande().getClient();
            if (client != null) {
                sb.append(" - ");
                if (client.getPrenom() != null) {
                    sb.append(client.getPrenom()).append(' ');
                }
                if (client.getNom() != null) {
                    sb.append(client.getNom());
                }
            }
        }
        if (rendezVous.getStatut() != null && rendezVous.getStatut().getLibelle() != null) {
            if (sb.length() > 0) {
                sb.append(" - ");
            }
            sb.append(rendezVous.getStatut().getLibelle());
        }
        return sb.length() == 0 ? "Rendez-vous" : sb.toString();
    }

    private String defaultGarageName() {
        return garageProperties != null && garageProperties.getName() != null
                ? garageProperties.getName()
                : "JLH Auto Pam";
    }

    private String defaultGarageAddress() {
        return garageProperties != null && garageProperties.getAddress() != null
                ? garageProperties.getAddress()
                : "JLH Auto Pam";
    }

    private DemandeResponse enrichDemandeResponse(Demande demande, DemandeResponse response) {
        if (demande == null || response == null) {
            return response;
        }
        devisRepository.findByDemande_IdDemande(demande.getIdDemande())
                .map(devisMapper::toResponse)
                .ifPresent(response::setDevis);

        RendezVous rendezVous = demande.getRendezVous();
        if (rendezVous != null) {
            response.setRendezVous(rendezVousMapper.toResponse(rendezVous));
        }
        return response;
    }

    private void applyServiceUpdates(Demande demande, List<com.jlh.jlhautopambackend.dto.DemandeServiceDto> services) {
        if (demande == null || services == null) {
            return;
        }
        Integer demandeId = demande.getIdDemande();
        services.forEach(serviceDto -> {
            if (serviceDto == null) {
                return;
            }
            Integer serviceId = serviceDto.getIdService();
            if (serviceId == null) {
                return;
            }
            Integer targetDemandeId = serviceDto.getIdDemande() != null ? serviceDto.getIdDemande() : demandeId;
            if (targetDemandeId == null) {
                return;
            }
            var key = new com.jlh.jlhautopambackend.modeles.DemandeServiceKey(targetDemandeId, serviceId);
            demandeServiceRepository.findById(key).ifPresent(entity -> {
                if (serviceDto.getQuantite() != null && serviceDto.getQuantite() > 0) {
                    entity.setQuantite(serviceDto.getQuantite());
                }
                if (serviceDto.getPrixUnitaire() != null) {
                    entity.setPrixUnitaireService(serviceDto.getPrixUnitaire());
                } else if (serviceDto.getPrixUnitaireService() != null) {
                    entity.setPrixUnitaireService(serviceDto.getPrixUnitaireService());
                }
                if (serviceDto.getRendezVousId() != null) {
                    entity.setRendezVousId(serviceDto.getRendezVousId());
                }
                demandeServiceRepository.save(entity);
            });
        });
    }
}
