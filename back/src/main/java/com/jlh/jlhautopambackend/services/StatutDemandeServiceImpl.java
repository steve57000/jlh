package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.StatutDemandeDto;
import com.jlh.jlhautopambackend.mapper.StatutDemandeMapper;
import com.jlh.jlhautopambackend.modeles.StatutDemande;
import com.jlh.jlhautopambackend.repository.StatutDemandeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StatutDemandeServiceImpl implements StatutDemandeService {
    private final StatutDemandeRepository repo;
    private final StatutDemandeMapper mapper;

    public StatutDemandeServiceImpl(StatutDemandeRepository repo, StatutDemandeMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public List<StatutDemandeDto> findAll() {
        return repo.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<StatutDemandeDto> findByCode(String codeStatut) {
        return repo.findById(codeStatut)
                .map(mapper::toDto);
    }

    @Override
    public StatutDemandeDto create(StatutDemandeDto dto) {
        if (repo.existsById(dto.getCodeStatut())) {
            throw new IllegalStateException("Statut_demande déjà existant : " + dto.getCodeStatut());
        }
        StatutDemande entity = mapper.toEntity(dto);
        StatutDemande saved = repo.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public Optional<StatutDemandeDto> update(String codeStatut, StatutDemandeDto dto) {
        return repo.findById(codeStatut)
                .map(existing -> {
                    existing.setLibelle(dto.getLibelle());
                    StatutDemande updated = repo.save(existing);
                    return mapper.toDto(updated);
                });
    }

    @Override
    public boolean delete(String codeStatut) {
        if (!repo.existsById(codeStatut)) {
            return false;
        }
        repo.deleteById(codeStatut);
        return true;
    }
}
