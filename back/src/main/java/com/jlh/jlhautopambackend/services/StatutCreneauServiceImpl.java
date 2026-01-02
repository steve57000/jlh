package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.StatutCreneauDto;
import com.jlh.jlhautopambackend.mapper.StatutCreneauMapper;
import com.jlh.jlhautopambackend.modeles.StatutCreneau;
import com.jlh.jlhautopambackend.repository.StatutCreneauRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StatutCreneauServiceImpl implements StatutCreneauService {
    private final StatutCreneauRepository repo;
    private final StatutCreneauMapper mapper;

    public StatutCreneauServiceImpl(StatutCreneauRepository repo, StatutCreneauMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public List<StatutCreneauDto> findAll() {
        return repo.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<StatutCreneauDto> findByCode(String codeStatut) {
        return repo.findById(codeStatut)
                .map(mapper::toDto);
    }

    @Override
    public StatutCreneauDto create(StatutCreneauDto dto) {
        if (repo.existsById(dto.getCodeStatut())) {
            throw new IllegalStateException("Statut déjà existant : " + dto.getCodeStatut());
        }
        StatutCreneau entity = mapper.toEntity(dto);
        StatutCreneau saved = repo.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public Optional<StatutCreneauDto> update(String codeStatut, StatutCreneauDto dto) {
        return repo.findById(codeStatut)
                .map(existing -> {
                    existing.setLibelle(dto.getLibelle());
                    StatutCreneau updated = repo.save(existing);
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
