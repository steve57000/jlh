package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.AvisServiceModerationRequest;
import com.jlh.jlhautopambackend.dto.AvisServiceRequest;
import com.jlh.jlhautopambackend.dto.AvisServiceResponse;
import com.jlh.jlhautopambackend.dto.AvisServiceUpdateRequest;
import com.jlh.jlhautopambackend.dto.ServiceAvisStatsResponse;
import com.jlh.jlhautopambackend.mapper.AvisServiceMapper;
import com.jlh.jlhautopambackend.modeles.AvisService;
import com.jlh.jlhautopambackend.modeles.AvisServiceStatut;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.DemandeService;
import com.jlh.jlhautopambackend.repository.AvisServiceRepository;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import com.jlh.jlhautopambackend.repository.DemandeServiceRepository;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AvisServiceServiceImpl implements AvisServiceService {

    private final AvisServiceRepository avisRepository;
    private final DemandeRepository demandeRepository;
    private final DemandeServiceRepository demandeServiceRepository;
    private final ClientRepository clientRepository;
    private final AvisServiceMapper mapper;

    public AvisServiceServiceImpl(
            AvisServiceRepository avisRepository,
            DemandeRepository demandeRepository,
            DemandeServiceRepository demandeServiceRepository,
            ClientRepository clientRepository,
            AvisServiceMapper mapper
    ) {
        this.avisRepository = avisRepository;
        this.demandeRepository = demandeRepository;
        this.demandeServiceRepository = demandeServiceRepository;
        this.clientRepository = clientRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AvisServiceResponse> findApproved(Pageable pageable) {
        return avisRepository.findByStatut(AvisServiceStatut.APPROVED, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AvisServiceResponse> findAll(Pageable pageable) {
        return avisRepository.findAll(pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public AvisServiceResponse create(Integer clientId, AvisServiceRequest request) {
        if (clientId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Client non authentifié.");
        }
        Demande demande = demandeRepository.findById(request.getDemandeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande introuvable."));
        if (demande.getClient() == null || !clientId.equals(demande.getClient().getIdClient())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action non autorisée.");
        }
        if (!"Traitee".equals(demande.getStatutDemande().getCodeStatut())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le service n'est pas encore terminé.");
        }
        List<DemandeService> services = demandeServiceRepository.findByDemande_IdDemande(demande.getIdDemande());
        if (services.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Aucun service n'est associé à cette demande.");
        }
        com.jlh.jlhautopambackend.modeles.Service service = services.get(0).getService();
        if (service == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Service introuvable.");
        }
        if (avisRepository.existsByDemande_IdDemandeAndService_IdService(demande.getIdDemande(), service.getIdService())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un avis existe déjà pour ce service.");
        }
        var client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client introuvable."));
        AvisService avis = AvisService.builder()
                .demande(demande)
                .service(service)
                .client(client)
                .note(request.getNote())
                .commentaire(request.getCommentaire())
                .creeLe(Instant.now())
                .statut(AvisServiceStatut.PENDING)
                .build();
        AvisService saved = avisRepository.save(avis);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvisServiceResponse> findByService(Integer serviceId) {
        return avisRepository.findByService_IdService(serviceId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvisServiceResponse> findByDemande(Integer demandeId) {
        return avisRepository.findByDemande_IdDemande(demandeId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvisServiceResponse> findByClient(Integer clientId) {
        return avisRepository.findByClient_IdClient(clientId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AvisServiceResponse> findByService(Integer serviceId, Pageable pageable) {
        return avisRepository.findByService_IdService(serviceId, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AvisServiceResponse> findByService(Integer serviceId, AvisServiceStatut statut, Pageable pageable) {
        return avisRepository.findByService_IdServiceAndStatut(serviceId, statut, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AvisServiceResponse> findApprovedByService(Integer serviceId, Pageable pageable) {
        return avisRepository.findByService_IdServiceAndStatut(serviceId, AvisServiceStatut.APPROVED, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AvisServiceResponse> findByDemande(Integer demandeId, Pageable pageable) {
        return avisRepository.findByDemande_IdDemande(demandeId, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AvisServiceResponse> findByClient(Integer clientId, Pageable pageable) {
        return avisRepository.findByClient_IdClient(clientId, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AvisServiceResponse findById(Long idAvis) {
        AvisService avis = avisRepository.findById(idAvis)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis introuvable."));
        return mapper.toResponse(avis);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceAvisStatsResponse getServiceStats(Integer serviceId) {
        var stats = avisRepository.getServiceStats(serviceId, AvisServiceStatut.APPROVED);
        Double moyenne = stats.map(AvisServiceRepository.ServiceAvisStats::getAverageNote).orElse(0.0);
        Long total = stats.map(AvisServiceRepository.ServiceAvisStats::getTotalAvis).orElse(0L);
        return ServiceAvisStatsResponse.builder()
                .serviceId(serviceId)
                .moyenneNote(moyenne != null ? moyenne : 0.0)
                .totalAvis(total != null ? total : 0L)
                .build();
    }

    @Override
    @Transactional
    public AvisServiceResponse updateByClient(Integer clientId, Long idAvis, AvisServiceUpdateRequest request) {
        if (clientId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Client non authentifié.");
        }
        AvisService avis = avisRepository.findById(idAvis)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis introuvable."));
        if (avis.getClient() == null || !clientId.equals(avis.getClient().getIdClient())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action non autorisée.");
        }
        if (AvisServiceStatut.APPROVED.equals(avis.getStatut())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "L'avis est déjà publié.");
        }
        avis.setNote(request.getNote());
        avis.setCommentaire(request.getCommentaire());
        avis.setStatut(AvisServiceStatut.PENDING);
        avis.setMotifRefus(null);
        AvisService saved = avisRepository.save(avis);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AvisServiceResponse moderate(Long idAvis, AvisServiceModerationRequest request) {
        AvisService avis = avisRepository.findById(idAvis)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis introuvable."));
        AvisServiceStatut statut;
        try {
            statut = AvisServiceStatut.valueOf(request.getStatut());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Statut invalide.");
        }
        if (AvisServiceStatut.REJECTED.equals(statut)
                && (request.getMotifRefus() == null || request.getMotifRefus().trim().isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le motif de refus est requis.");
        }
        if (AvisServiceStatut.PENDING.equals(statut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le statut en attente n'est pas une action valide.");
        }
        avis.setStatut(statut);
        avis.setMotifRefus(AvisServiceStatut.REJECTED.equals(statut) ? request.getMotifRefus() : null);
        AvisService saved = avisRepository.save(avis);
        return mapper.toResponse(saved);
    }
}
