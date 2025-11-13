package com.jlh.jlhautopambackend.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.DemandeService;
import com.jlh.jlhautopambackend.modeles.DemandeServiceKey;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import com.jlh.jlhautopambackend.repository.DemandeServiceRepository;
import com.jlh.jlhautopambackend.repository.ServiceRepository;
import com.jlh.jlhautopambackend.dto.DemandeServiceRequest;
import com.jlh.jlhautopambackend.dto.DemandeServiceResponse;
import com.jlh.jlhautopambackend.mapper.DemandeServiceMapper;

@Service
@Transactional
public class DemandeServiceServiceImpl implements DemandeServiceService {

    private final DemandeServiceRepository dsRepo;
    private final DemandeRepository demandeRepo;
    private final ServiceRepository serviceRepo;
    private final DemandeServiceMapper mapper;

    public DemandeServiceServiceImpl(
            DemandeServiceRepository dsRepo,
            DemandeRepository demandeRepo,
            ServiceRepository serviceRepo,
            DemandeServiceMapper mapper
    ) {
        this.dsRepo = dsRepo;
        this.demandeRepo = demandeRepo;
        this.serviceRepo = serviceRepo;
        this.mapper = mapper;
    }

    @Override
    public List<DemandeServiceResponse> findAll() {
        return dsRepo.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DemandeServiceResponse> findByKey(Integer demandeId, Integer serviceId) {
        DemandeServiceKey key = new DemandeServiceKey(demandeId, serviceId);
        return dsRepo.findById(key)
                .map(mapper::toDto);
    }

    @Override
    public DemandeServiceResponse create(DemandeServiceRequest req) {
        var demande = demandeRepo.findById(req.getDemandeId())
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
        var serviceEntity = serviceRepo.findByIdServiceAndArchivedFalse(req.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Service introuvable"));

        var key = new DemandeServiceKey(req.getDemandeId(), req.getServiceId());
        var existing = dsRepo.findById(key).orElse(null);

        int requestedQty = req.getQuantite() == null ? 1 : Math.max(1, req.getQuantite());
        Integer maxQty = serviceEntity.getQuantiteMax();
        if (maxQty != null && maxQty > 0 && requestedQty > maxQty) {
            throw new IllegalArgumentException("Quantité demandée supérieure au maximum autorisé pour ce service.");
        }

        if (existing != null) {
            int base = existing.getQuantite() == null ? 0 : existing.getQuantite();
            int q = base + requestedQty;
            if (maxQty != null && maxQty > 0 && q > maxQty) {
                throw new IllegalArgumentException("La quantité totale dépasse la limite pour ce service.");
            }
            existing.setQuantite(q);
            existing.snapshotFromService(serviceEntity);
            return mapper.toDto(dsRepo.save(existing));
        }

        DemandeService entity = mapper.toEntity(req);
        // defaults robustes
        entity.setQuantite(requestedQty);
        entity.setDemande(demande);
        entity.setService(serviceEntity);
        entity.snapshotFromService(serviceEntity);

        return mapper.toDto(dsRepo.save(entity));
    }

    @Override
    public Optional<DemandeServiceResponse> update(Integer demandeId, Integer serviceId, DemandeServiceRequest req) {
        DemandeServiceKey key = new DemandeServiceKey(demandeId, serviceId);
        return dsRepo.findById(key)
                .map(entity -> {
                    int requested = req.getQuantite() == null ? 1 : Math.max(1, req.getQuantite());
                    Integer maxQty = entity.getService() != null ? entity.getService().getQuantiteMax() : null;
                    if (maxQty != null && maxQty > 0 && requested > maxQty) {
                        throw new IllegalArgumentException("Quantité demandée supérieure au maximum autorisé pour ce service.");
                    }
                    entity.setQuantite(requested);
                    if (entity.getService() != null) {
                        entity.snapshotFromService(entity.getService());
                    }
                    DemandeService saved = dsRepo.save(entity);
                    return mapper.toDto(saved);
                });
    }

    @Override
    public boolean delete(Integer demandeId, Integer serviceId) {
        DemandeServiceKey key = new DemandeServiceKey(demandeId, serviceId);
        if (!dsRepo.existsById(key)) {
            return false;
        }
        dsRepo.deleteById(key);

        // 🔍 Combien de lignes restent ?
        long remaining = dsRepo.countByDemande_IdDemande(demandeId);
        if (remaining == 0) {
            // Si la demande est un brouillon -> on la supprime
            demandeRepo.findById(demandeId).ifPresent(d -> {
                String code = d.getStatutDemande() != null ? d.getStatutDemande().getCodeStatut() : null;
                if ("Brouillon".equals(code)) {
                    demandeRepo.deleteById(demandeId);
                }
            });
        }
        return true;
    }
}
