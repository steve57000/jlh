package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ServiceRequest;
import com.jlh.jlhautopambackend.dto.ServiceResponse;
import com.jlh.jlhautopambackend.mapper.ServiceMapper;
import com.jlh.jlhautopambackend.modeles.Service;
import com.jlh.jlhautopambackend.repository.DemandeServiceRepository;
import com.jlh.jlhautopambackend.repository.ServiceRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

// On utilise la forme fully-qualified pour l’annotation afin d’éviter le conflit de nom
@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository repo;
    private final DemandeServiceRepository demandeServiceRepository;
    private final ServiceMapper mapper;

    public ServiceServiceImpl(ServiceRepository repo,
                              DemandeServiceRepository demandeServiceRepository,
                              ServiceMapper mapper) {
        this.repo = repo;
        this.demandeServiceRepository = demandeServiceRepository;
        this.mapper = mapper;
    }

    @Override
    public List<ServiceResponse> findAll() {
        return repo.findAllByArchivedFalseOrderByLibelleAsc().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public Optional<ServiceResponse> findById(Integer id) {
        return repo.findByIdServiceAndArchivedFalse(id)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public ServiceResponse create(ServiceRequest request) {
        Service toSave = mapper.toEntity(request);
        if (toSave.getQuantiteMax() == null || toSave.getQuantiteMax() < 1) {
            toSave.setQuantiteMax(1);
        }
        Service saved  = repo.save(toSave);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public Optional<ServiceResponse> update(Integer id, ServiceRequest request) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setLibelle(request.getLibelle());
                    existing.setDescription(request.getDescription());
                    existing.setPrixUnitaire(request.getPrixUnitaire());
                    Integer newMax = request.getQuantiteMax();
                    if (newMax == null || newMax < 1) {
                        newMax = 1;
                    }
                    existing.setQuantiteMax(newMax);
                    existing.setArchived(false);
                    Service saved = repo.save(existing);
                    var associations = demandeServiceRepository.findByService_IdService(id);
                    associations.forEach(ds -> {
                        ds.snapshotFromService(saved);
                        if (saved.getQuantiteMax() != null && saved.getQuantiteMax() > 0) {
                            int current = ds.getQuantite() != null ? ds.getQuantite() : 1;
                            if (current > saved.getQuantiteMax()) {
                                ds.setQuantite(saved.getQuantiteMax());
                            }
                        }
                    });
                    demandeServiceRepository.saveAll(associations);
                    return mapper.toResponse(saved);
                });
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        return repo.findById(id)
                .map(service -> {
                    if (!service.isArchived()) {
                        var associations = demandeServiceRepository.findByService_IdService(id);
                        associations.forEach(ds -> ds.snapshotFromService(service));
                        demandeServiceRepository.saveAll(associations);
                        service.setArchived(true);
                        repo.save(service);
                    }
                    return true;
                })
                .orElse(false);
    }
}
