package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RendezVousMapper {

    @Mapping(target = "idRdv", ignore = true)
    @Mapping(target = "demande", ignore = true)
    @Mapping(target = "creneau", ignore = true)
    @Mapping(target = "administrateur", ignore = true)
    @Mapping(target = "statut", ignore = true)
    RendezVous toEntity(RendezVousRequest dto);

    @Mapping(target = "idRdv",           source = "entity.idRdv")
    @Mapping(target = "demandeId",       source = "entity.demande.idDemande")
    @Mapping(target = "creneauId",       source = "entity.creneau.idCreneau")
    @Mapping(target = "administrateurId",source = "entity.administrateur.idAdmin")
    @Mapping(target = "statut",          expression = "java(toDtoStatut(entity.getStatut()))")
    RendezVousResponse toResponse(RendezVous entity);

    default StatutRendezVousDto toDtoStatut(StatutRendezVous s) {
        if (s == null) return null;
        return StatutRendezVousDto.builder()
                .codeStatut(s.getCodeStatut())
                .libelle(s.getLibelle())
                .build();
    }
}
