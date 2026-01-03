package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.TypeDemandeDto;

import java.util.List;
import java.util.Optional;

public interface TypeDemandeService {
    List<TypeDemandeDto> findAll();
    Optional<TypeDemandeDto> findById(String code);
    TypeDemandeDto create(TypeDemandeDto dto);
    Optional<TypeDemandeDto> update(String code, TypeDemandeDto dto);
    boolean delete(String code);
}
