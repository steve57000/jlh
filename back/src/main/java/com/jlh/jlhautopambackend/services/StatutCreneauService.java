package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.StatutCreneauDto;
import java.util.List;
import java.util.Optional;

public interface StatutCreneauService {
    List<StatutCreneauDto> findAll();
    Optional<StatutCreneauDto> findByCode(String codeStatut);
    StatutCreneauDto create(StatutCreneauDto dto);
    Optional<StatutCreneauDto> update(String codeStatut, StatutCreneauDto dto);
    boolean delete(String codeStatut);
}