package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.StatutDemandeDto;
import java.util.List;
import java.util.Optional;

public interface StatutDemandeService {
    List<StatutDemandeDto> findAll();
    Optional<StatutDemandeDto> findByCode(String codeStatut);
    StatutDemandeDto create(StatutDemandeDto dto);
    Optional<StatutDemandeDto> update(String codeStatut, StatutDemandeDto dto);
    boolean delete(String codeStatut);
}