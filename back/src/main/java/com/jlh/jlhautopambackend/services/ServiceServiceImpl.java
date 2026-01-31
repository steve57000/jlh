package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ServiceRequest;
import com.jlh.jlhautopambackend.dto.ServiceResponse;
import com.jlh.jlhautopambackend.mapper.ServiceMapper;
import com.jlh.jlhautopambackend.modeles.Service;
import com.jlh.jlhautopambackend.modeles.ServicePrixMode;
import com.jlh.jlhautopambackend.modeles.ServiceQuantiteMode;
import com.jlh.jlhautopambackend.repository.ServiceRepository;

import java.util.List;
import java.util.Optional;

// On utilise la forme fully-qualified pour l’annotation afin d’éviter le conflit de nom
@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository repo;
    private final ServiceMapper mapper;
    private final ServiceIconService iconService;

    public ServiceServiceImpl(ServiceRepository repo, ServiceMapper mapper, ServiceIconService iconService) {
        this.repo   = repo;
        this.mapper = mapper;
        this.iconService = iconService;
    }

    @Override
    public List<ServiceResponse> findAll() {
        return repo.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public Optional<ServiceResponse> findById(Integer id) {
        return repo.findById(id)
                .map(mapper::toResponse);
    }

    @Override
    public ServiceResponse create(ServiceRequest request) {
        Service toSave = mapper.toEntity(request);
        applyServiceConfiguration(toSave, request);
        toSave.setIcon(iconService.resolveIcon(request.getIconId()));
        Service saved  = repo.save(toSave);
        return mapper.toResponse(saved);
    }

    @Override
    public Optional<ServiceResponse> update(Integer id, ServiceRequest request) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setLibelle(request.getLibelle());
                    existing.setDescription(request.getDescription());
                    existing.setDescriptionLongue(request.getDescriptionLongue());
                    existing.setIcon(iconService.resolveIcon(request.getIconId()));
                    existing.setPrixUnitaire(request.getPrixUnitaire());
                    applyServiceConfiguration(existing, request);
                    Service saved = repo.save(existing);
                    return mapper.toResponse(saved);
                });
    }

    @Override
    public boolean delete(Integer id) {
        if (!repo.existsById(id)) {
            return false;
        }
        repo.deleteById(id);
        return true;
    }

    private void applyServiceConfiguration(Service target, ServiceRequest request) {
        ServiceQuantiteMode quantiteMode = request.getQuantiteMode() != null
                ? request.getQuantiteMode()
                : (target.getQuantiteMode() != null ? target.getQuantiteMode() : ServiceQuantiteMode.UNIQUE);
        ServicePrixMode prixMode = request.getPrixMode() != null
                ? request.getPrixMode()
                : (target.getPrixMode() != null ? target.getPrixMode() : ServicePrixMode.UNITAIRE);
        Integer tailleLot = request.getTailleLot() != null ? request.getTailleLot() : target.getTailleLot();

        if (quantiteMode == ServiceQuantiteMode.LOT) {
            if (tailleLot == null || tailleLot < 1) {
                throw new IllegalArgumentException("Taille de lot requise pour un service par lot.");
            }
        } else {
            tailleLot = null;
        }

        if (prixMode == ServicePrixMode.LOT && (tailleLot == null || tailleLot < 1)) {
            throw new IllegalArgumentException("Taille de lot requise pour un prix par lot.");
        }

        Integer quantiteMax = request.getQuantiteMax() != null ? request.getQuantiteMax() : target.getQuantiteMax();
        if (quantiteMode == ServiceQuantiteMode.UNIQUE) {
            if (quantiteMax != null && quantiteMax > 1) {
                throw new IllegalArgumentException("Quantité maximale doit être 1 pour un service unique.");
            }
            quantiteMax = 1;
        } else if (quantiteMax != null && quantiteMax > 0 && tailleLot != null && quantiteMax % tailleLot != 0) {
            throw new IllegalArgumentException("Quantité maximale doit être un multiple de la taille du lot.");
        }

        target.setQuantiteMode(quantiteMode);
        target.setPrixMode(prixMode);
        target.setTailleLot(tailleLot);
        target.setQuantiteMax(quantiteMax);
    }
}
