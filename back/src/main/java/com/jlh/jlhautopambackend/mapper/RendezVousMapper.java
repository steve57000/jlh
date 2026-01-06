package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RendezVousMapper {

    @Mapping(target = "idRdv", ignore = true)
    @Mapping(target = "demande", ignore = true)
    @Mapping(target = "demandeService", ignore = true)
    @Mapping(target = "devis", ignore = true)
    @Mapping(target = "creneau", ignore = true)
    @Mapping(target = "administrateur", ignore = true)
    @Mapping(target = "statut", ignore = true)
    RendezVous toEntity(RendezVousRequest dto);

    @Mapping(target = "idRdv",           source = "entity.idRdv")
    @Mapping(target = "demandeId",       source = "entity.demande.idDemande")
    @Mapping(target = "serviceId",       source = "entity.demandeService.service.idService")
    @Mapping(target = "devisId",         source = "entity.devis.idDevis")
    @Mapping(target = "clientId",        source = "entity.demande.client.idClient")
    @Mapping(target = "creneauId",       source = "entity.creneau.idCreneau")
    @Mapping(target = "dateDebut",       source = "entity.creneau.dateDebut")
    @Mapping(target = "dateFin",         source = "entity.creneau.dateFin")
    @Mapping(target = "administrateurId",source = "entity.administrateur.idAdmin")
    @Mapping(target = "statut",          expression = "java(toDtoStatut(entity.getStatut()))")
    @Mapping(target = "commentaire",     source = "entity.commentaire")
    RendezVousResponse toResponse(RendezVous entity);

    default StatutRendezVousDto toDtoStatut(StatutRendezVous s) {
        if (s == null) return null;
        return StatutRendezVousDto.builder()
                .codeStatut(s.getCodeStatut())
                .libelle(s.getLibelle())
                .build();
    }
}
