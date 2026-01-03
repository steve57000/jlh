package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.StatutRendezVousDto;
import com.jlh.jlhautopambackend.mapper.StatutRendezVousMapper;
import com.jlh.jlhautopambackend.modeles.StatutRendezVous;
import com.jlh.jlhautopambackend.repository.StatutRendezVousRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StatutRendezVousServiceImpl implements StatutRendezVousService {

    private final StatutRendezVousRepository repo;
    private final StatutRendezVousMapper mapper;

    public StatutRendezVousServiceImpl(StatutRendezVousRepository repo,
                                       StatutRendezVousMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public List<StatutRendezVousDto> findAll() {
        return repo.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<StatutRendezVousDto> findByCode(String code) {
        return repo.findById(code).map(mapper::toDto);
    }

    @Override
    public StatutRendezVousDto create(StatutRendezVousDto dto) {
        StatutRendezVous saved = repo.save(mapper.toEntity(dto));
        return mapper.toDto(saved);
    }

    @Override
    public Optional<StatutRendezVousDto> update(String code, StatutRendezVousDto dto) {
        return repo.findById(code)
                .map(entity -> {
                    mapper.updateEntity(dto, entity);
                    return mapper.toDto(repo.save(entity));
                });
    }

    @Override
    public boolean delete(String code) {
        if (!repo.existsById(code)) return false;
        repo.deleteById(code);
        return true;
    }
}
