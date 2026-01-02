package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.TypeDemandeDto;
import com.jlh.jlhautopambackend.mapper.TypeDemandeMapper;
import com.jlh.jlhautopambackend.modeles.TypeDemande;
import com.jlh.jlhautopambackend.repository.TypeDemandeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TypeDemandeServiceImpl implements TypeDemandeService {

    private final TypeDemandeRepository repo;
    private final TypeDemandeMapper mapper;

    public TypeDemandeServiceImpl(TypeDemandeRepository repo, TypeDemandeMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public List<TypeDemandeDto> findAll() {
        return repo.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TypeDemandeDto> findById(String code) {
        return repo.findById(code).map(mapper::toDto);
    }

    @Override
    public TypeDemandeDto create(TypeDemandeDto dto) {
        if (repo.existsById(dto.getCodeType())) {
            throw new IllegalArgumentException("TypeDemande déjà existant");
        }
        TypeDemande saved = repo.save(mapper.toEntity(dto));
        return mapper.toDto(saved);
    }

    @Override
    public Optional<TypeDemandeDto> update(String code, TypeDemandeDto dto) {
        return repo.findById(code).map(existing -> {
            existing.setLibelle(dto.getLibelle());
            TypeDemande updated = repo.save(existing);
            return mapper.toDto(updated);
        });
    }

    @Override
    public boolean delete(String code) {
        if (!repo.existsById(code)) {
            return false;
        }
        repo.deleteById(code);
        return true;
    }
}
