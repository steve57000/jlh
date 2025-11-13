package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.TypeDemandeDto;
import com.jlh.jlhautopambackend.modeles.TypeDemande;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public class TypeDemandeMapper {

    public TypeDemandeDto toDto(TypeDemande entity) {
        return TypeDemandeDto.builder()
                .codeType(entity.getCodeType())
                .libelle(entity.getLibelle())
                .build();
    }

    public TypeDemande toEntity(TypeDemandeDto dto) {
        return TypeDemande.builder()
                .codeType(dto.getCodeType())
                .libelle(dto.getLibelle())
                .build();
    }
}
