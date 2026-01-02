package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.StatutDemandeDto;
import com.jlh.jlhautopambackend.modeles.StatutDemande;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StatutDemandeMapper {
    StatutDemande toEntity(StatutDemandeDto dto);
    StatutDemandeDto toDto(StatutDemande entity);
}

