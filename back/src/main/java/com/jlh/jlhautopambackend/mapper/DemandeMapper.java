package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.DemandeDto;
import com.jlh.jlhautopambackend.modeles.Demande;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {ClientMapper.class, DemandeServiceMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DemandeMapper {

    @Mapping(target = "codeType", source = "typeDemande.codeType")
    @Mapping(target = "typeLibelle", source = "typeDemande.libelle")
    @Mapping(target = "codeStatut", source = "statutDemande.codeStatut")
    @Mapping(target = "statutLibelle", source = "statutDemande.libelle")
    DemandeDto toDto(Demande entity);

    List<DemandeDto> toDtos(List<Demande> entities);
}
