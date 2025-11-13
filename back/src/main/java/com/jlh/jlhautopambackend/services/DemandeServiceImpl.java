package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.config.GarageProperties;
import com.jlh.jlhautopambackend.dto.ClientStatsDto;
import com.jlh.jlhautopambackend.dto.DemandeRequest;
import com.jlh.jlhautopambackend.dto.DemandeResponse;
import com.jlh.jlhautopambackend.dto.DemandeServiceDto;
import com.jlh.jlhautopambackend.dto.ProchainRdvDto;
import com.jlh.jlhautopambackend.mapper.DemandeMapper;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.DemandeService;
import com.jlh.jlhautopambackend.modeles.StatutDemande;
import com.jlh.jlhautopambackend.modeles.TypeDemande;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import com.jlh.jlhautopambackend.repository.DemandeServiceRepository;
import com.jlh.jlhautopambackend.repository.RendezVousRepository;
import com.jlh.jlhautopambackend.repository.StatutDemandeRepository;
import com.jlh.jlhautopambackend.repository.TypeDemandeRepository;
import com.jlh.jlhautopambackend.utils.IcsUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DemandeServiceImpl implements DemandeService {

    private final DemandeRepository       repo;
    private final ClientRepository        clientRepo;
    private final TypeDemandeRepository   typeRepo;
    private final StatutDemandeRepository statutRepo;
    private final DemandeMapper           mapper;
    private final RendezVousRepository    rendezVousRepo;
    private final GarageProperties        garageProps;
    private final DemandeServiceRepository demandeServiceRepository;

    public DemandeServiceImpl(
            DemandeRepository repo,
            ClientRepository clientRepo,
            TypeDemandeRepository typeRepo,
            StatutDemandeRepository statutRepo,
            DemandeMapper mapper,
            RendezVousRepository rendezVousRepo,
            GarageProperties garageProps,
            DemandeServiceRepository demandeServiceRepository
    ) {
        this.repo = repo;
        this.clientRepo = clientRepo;
        this.typeRepo = typeRepo;
        this.statutRepo = statutRepo;
        this.mapper = mapper;
        this.rendezVousRepo = rendezVousRepo;
        this.garageProps = garageProps;
        this.demandeServiceRepository = demandeServiceRepository;
    }

    private static final String STATUT_BROUILLON  = "Brouillon";
    private static final String STATUT_EN_ATTENTE = "En_attente";
    private static final String TYPE_DEVIS        = "Devis";

    private TypeDemande getTypeOrThrow(String code) {
        return typeRepo.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("TypeDemande introuvable: " + code));
    }

    private StatutDemande getStatutOrThrow(String code) {
        return statutRepo.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("StatutDemande introuvable: " + code));
    }

    private TypeDemande getDefaultType() {
        return getTypeOrThrow(TYPE_DEVIS);
    }

    /** ⬅️ Défaut = Brouillon (panier temporaire tant que non validé) */
    private StatutDemande getDefaultStatut() {
        return getStatutOrThrow(STATUT_BROUILLON);
    }

    private void ensureDateDemandeSet(Demande entity) {
        if (entity.getDateDemande() == null) {
            entity.setDateDemande(Instant.now());
        }
    }

    @Override
    public DemandeResponse create(DemandeRequest request) {
        Demande entity = mapper.toEntity(request);

        Client client = clientRepo.findById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable : " + request.getClientId()));
        entity.setClient(client);

        String codeType   = (request.getCodeType()   != null && !request.getCodeType().isBlank()) ? request.getCodeType()   : TYPE_DEVIS;
        // ⬇️ si non fourni -> Brouillon (et non En_attente)
        String codeStatut = (request.getCodeStatut() != null && !request.getCodeStatut().isBlank()) ? request.getCodeStatut() : STATUT_BROUILLON;

        entity.setTypeDemande(getTypeOrThrow(codeType));
        entity.setStatutDemande(getStatutOrThrow(codeStatut));

        ensureDateDemandeSet(entity);

        return mapper.toResponse(repo.save(entity));
    }

    /** (utilisé si jamais tu gardes createPublic) */
    @Override
    public DemandeResponse createPublic() {
        Demande entity = new Demande();
        entity.setTypeDemande(getDefaultType());
        entity.setStatutDemande(getDefaultStatut()); // ⬅️ Brouillon
        ensureDateDemandeSet(entity);
        return mapper.toResponse(repo.save(entity));
    }

    @Override
    public DemandeResponse createForClient(Integer clientId, DemandeRequest request) {
        Demande entity = mapper.toEntity(request);

        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable : " + clientId));
        entity.setClient(client);

        String codeType   = (request.getCodeType()   != null && !request.getCodeType().isBlank()) ? request.getCodeType()   : TYPE_DEVIS;
        String codeStatut = (request.getCodeStatut() != null && !request.getCodeStatut().isBlank()) ? request.getCodeStatut() : STATUT_BROUILLON;

        entity.setTypeDemande(getTypeOrThrow(codeType));
        entity.setStatutDemande(getStatutOrThrow(codeStatut));

        ensureDateDemandeSet(entity);

        return mapper.toResponse(repo.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> findCurrentIdForClient(Integer clientId) {
        // ⚠️ on cherche bien la demande "Brouillon" (le panier)
        return repo.findFirstByClient_IdClientAndStatutDemande_CodeStatutOrderByDateDemandeDesc(clientId, STATUT_BROUILLON)
                .map(Demande::getIdDemande);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DemandeResponse> findCurrentForClient(Integer clientId) {
        return repo.findFirstByClient_IdClientAndStatutDemande_CodeStatutOrderByDateDemandeDesc(clientId, STATUT_BROUILLON)
                .map(mapper::toResponse);
    }

    @Override
    public DemandeResponse getOrCreateForClient(Integer clientId) {
        return findCurrentForClient(clientId).orElseGet(() -> {
            var type   = getDefaultType();
            var statut = getDefaultStatut(); // ⬅️ Brouillon
            var client = clientRepo.findById(clientId)
                    .orElseThrow(() -> new IllegalArgumentException("Client introuvable: " + clientId));

            Demande entity = new Demande();
            entity.setDateDemande(Instant.now());
            entity.setClient(client);
            entity.setTypeDemande(type);
            entity.setStatutDemande(statut);

            return mapper.toResponse(repo.save(entity));
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DemandeResponse> findById(Integer id) {
        return repo.findById(id).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeResponse> findAll() {
        return repo.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeResponse> findByClientId(Integer clientId) {
        return repo.findByClient_IdClient(clientId)
                .stream()
                .map(mapper::toResponse)
                // ⛔️ on masque tout brouillon sans service par sécurité
                .filter(d -> {
                    String code = d.getStatutDemande() != null ? d.getStatutDemande().getCodeStatut() : null;
                    int n = d.getServices() != null ? d.getServices().size() : 0;
                    return !("Brouillon".equals(code) && n == 0);
                })
                .toList();
    }

    @Override
    public Optional<DemandeResponse> update(Integer id, DemandeRequest request) {
        return repo.findById(id).map(existing -> {
            if (request.getDateDemande() != null) {
                existing.setDateDemande(request.getDateDemande());
            }

            if (request.getClientId() != null) {
                existing.setClient(
                        clientRepo.findById(request.getClientId())
                                .orElseThrow(() -> new IllegalArgumentException("Client introuvable: " + request.getClientId()))
                );
            }

            if (request.getCodeType() != null && !request.getCodeType().isBlank()) {
                existing.setTypeDemande(getTypeOrThrow(request.getCodeType()));
            }
            if (request.getCodeStatut() != null && !request.getCodeStatut().isBlank()) {
                existing.setStatutDemande(getStatutOrThrow(request.getCodeStatut()));
            }

            updateClientContact(existing, request);
            List<DemandeService> updatedLines = updateServiceLines(existing, request.getServices());

            ensureDateDemandeSet(existing);
            Demande saved = repo.save(existing);
            if (!updatedLines.isEmpty()) {
                demandeServiceRepository.saveAll(updatedLines);
            }

            return mapper.toResponse(saved);
        });
    }

    private void updateClientContact(Demande demande, DemandeRequest request) {
        Client client = demande.getClient();
        if (client == null) {
            return;
        }

        boolean dirty = false;

        if (request.getImmatriculation() != null && !request.getImmatriculation().isBlank()) {
            client.setImmatriculation(request.getImmatriculation().trim());
            dirty = true;
        }
        if (request.getTelephone() != null && !request.getTelephone().isBlank()) {
            client.setTelephone(request.getTelephone().trim());
            dirty = true;
        }
        if (request.getAdresseLigne1() != null) {
            String value = request.getAdresseLigne1();
            client.setAdresseLigne1(value != null && !value.isBlank() ? value.trim() : null);
            dirty = true;
        }
        if (request.getAdresseLigne2() != null) {
            String value = request.getAdresseLigne2();
            client.setAdresseLigne2(value != null && !value.isBlank() ? value.trim() : null);
            dirty = true;
        }
        if (request.getAdresseCodePostal() != null) {
            String value = request.getAdresseCodePostal();
            client.setAdresseCodePostal(value != null && !value.isBlank() ? value.trim() : null);
            dirty = true;
        }
        if (request.getAdresseVille() != null) {
            String value = request.getAdresseVille();
            client.setAdresseVille(value != null && !value.isBlank() ? value.trim() : null);
            dirty = true;
        }

        if (dirty) {
            clientRepo.save(client);
        }
    }

    private List<DemandeService> updateServiceLines(
            Demande demande,
            List<DemandeServiceDto> payload
    ) {
        if (payload == null || payload.isEmpty()) {
            return Collections.emptyList();
        }

        List<DemandeService> current =
                demande.getServices() != null ? demande.getServices() : Collections.emptyList();
        Map<Integer, DemandeService> indexed = new HashMap<>();
        for (DemandeService ds : current) {
            Integer sid = null;
            if (ds.getService() != null) {
                sid = ds.getService().getIdService();
            }
            if (sid == null && ds.getId() != null) {
                sid = ds.getId().getIdService();
            }
            if (sid != null) {
                indexed.putIfAbsent(sid, ds);
            }
        }

        List<DemandeService> updated = new ArrayList<>();

        for (DemandeServiceDto dto : payload) {
            if (dto == null || dto.getIdService() == null) {
                continue;
            }
            DemandeService line = indexed.get(dto.getIdService());
            if (line == null) {
                continue;
            }

            if (dto.getQuantite() != null) {
                int newQty = Math.max(1, dto.getQuantite());
                Integer max = line.getService() != null ? line.getService().getQuantiteMax() : null;
                if (max != null && max > 0 && newQty > max) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "La quantité demandée pour le service %s dépasse le maximum autorisé (%d).",
                                    line.getLibelleService() != null ? line.getLibelleService() : dto.getLibelle(),
                                    max
                            )
                    );
                }
                line.setQuantite(newQty);
            }

            if (dto.getLibelle() != null && !dto.getLibelle().isBlank()) {
                line.setLibelleService(dto.getLibelle().trim());
            }

            if (dto.getDescription() != null) {
                line.setDescriptionService(dto.getDescription());
            }

            BigDecimal prix = dto.getPrixUnitaire();
            if (prix != null) {
                line.setPrixUnitaireService(prix);
            }

            updated.add(line);
        }

        return updated;
    }

    @Override
    public boolean delete(Integer id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public ClientStatsDto findStatsByClientId(Integer clientId) {
        long enAttente = repo.countByClient_IdClientAndStatutDemande_CodeStatut(clientId, "En_attente");
        long traitees  = repo.countByClient_IdClientAndStatutDemande_CodeStatut(clientId, "Traitee");
        long annulees  = repo.countByClient_IdClientAndStatutDemande_CodeStatut(clientId, "Annulee");
        long rdvAvenir = rendezVousRepo.countUpcomingByClientId(clientId, Instant.now());
        return new ClientStatsDto(enAttente, traitees, annulees, rdvAvenir);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProchainRdvDto> findProchainRdvByClientId(Integer clientId) {
        var list = rendezVousRepo.findUpcomingByClientId(clientId, Instant.now());
        if (list.isEmpty()) return Optional.empty();
        var rv = list.get(0);
        return Optional.of(ProchainRdvDto.builder()
                .idRdv(rv.getIdRdv())
                .codeStatut(rv.getStatut().getCodeStatut())
                .libelleStatut(rv.getStatut().getLibelle())
                .dateDebut(rv.getCreneau().getDateDebut())
                .dateFin(rv.getCreneau().getDateFin())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> buildProchainRendezVousIcs(Integer clientId) {
        var list = rendezVousRepo.findUpcomingByClientId(clientId, Instant.now());
        if (list.isEmpty()) return Optional.empty();

        var rv = list.get(0);
        var cr = rv.getCreneau();

        String uid        = IcsUtil.uid("rv-" + rv.getIdRdv() + "-" + cr.getDateDebut());
        String garageName = (garageProps.getName() != null && !garageProps.getName().isBlank())
                ? garageProps.getName() : "JLH Auto Pam";
        String summary    = "Rendez-vous " + garageName;
        String description = "Statut: " + rv.getStatut().getLibelle();
        String location    = garageProps.getAddress() != null ? garageProps.getAddress() : "";

        String ics = IcsUtil.veventWithAlarms(
                uid, summary, description, location,
                cr.getDateDebut(), cr.getDateFin(),
                garageName, garageProps.getOrganizerEmail()
        );

        return Optional.of(ics);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> buildRendezVousIcs(Integer rdvId, Integer clientIdOrNullIfAdmin) {
        var opt = (clientIdOrNullIfAdmin == null)
                ? rendezVousRepo.findById(rdvId)
                : rendezVousRepo.findByIdAndClient(rdvId, clientIdOrNullIfAdmin);

        if (opt.isEmpty()) return Optional.empty();
        var rv = opt.get();
        var cr = rv.getCreneau();

        String uid        = IcsUtil.uid("rv-" + rv.getIdRdv() + "-" + cr.getDateDebut());
        String garageName = (garageProps.getName() != null && !garageProps.getName().isBlank())
                ? garageProps.getName() : "JLH Auto Pam";
        String summary    = "Rendez-vous " + garageName;
        String description = "Statut: " + rv.getStatut().getLibelle();
        String location    = garageProps.getAddress() != null ? garageProps.getAddress() : "";

        String ics = IcsUtil.veventWithAlarms(
                uid, summary, description, location,
                cr.getDateDebut(), cr.getDateFin(),
                garageName, garageProps.getOrganizerEmail()
        );

        return Optional.of(ics);
    }
}
