package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.StatutRendezVousDto;
import java.util.List;
import java.util.Optional;

public interface StatutRendezVousService {
    List<StatutRendezVousDto> findAll();
    Optional<StatutRendezVousDto> findByCode(String code);
    StatutRendezVousDto create(StatutRendezVousDto dto);
    Optional<StatutRendezVousDto> update(String code, StatutRendezVousDto dto);
    boolean delete(String code);
}
