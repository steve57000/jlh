package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.CreneauRequest;
import com.jlh.jlhautopambackend.dto.CreneauResponse;
import com.jlh.jlhautopambackend.mapper.CreneauMapper;
import com.jlh.jlhautopambackend.modeles.Creneau;
import com.jlh.jlhautopambackend.repository.CreneauRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CreneauServiceImpl implements CreneauService {

    private final CreneauRepository repository;
    private final CreneauMapper mapper;

    public CreneauServiceImpl(CreneauRepository repository,
                              CreneauMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CreneauResponse create(CreneauRequest request) {
        Creneau entity = mapper.toEntity(request);
        Creneau saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreneauResponse> findById(Integer id) {
        return repository.findById(id)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreneauResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CreneauResponse> update(Integer id, CreneauRequest request) {
        return repository.findById(id)
                .map(existing -> {
                    // mettre à jour les champs nécessaires
                    existing.setDateDebut(request.getDateDebut());
                    existing.setDateFin(request.getDateFin());
                    existing.setStatut(mapper.toEntity(request).getStatut());
                    Creneau updated = repository.save(existing);
                    return mapper.toResponse(updated);
                });
    }

    @Override
    public boolean delete(Integer id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}
