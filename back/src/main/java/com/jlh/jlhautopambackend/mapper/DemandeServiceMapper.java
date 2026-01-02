package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DemandeServiceMapper {

    @Mapping(source = "request.demandeId", target = "id.idDemande")
    @Mapping(source = "request.serviceId",  target = "id.idService")
    @Mapping(source = "request.quantite",   target = "quantite")
    @Mapping(source = "request.prixUnitaire", target = "prixUnitaireService")
    @Mapping(target = "demande", ignore = true) // on les pose en service
    @Mapping(target = "service", ignore = true)
    DemandeService toEntity(DemandeServiceRequest request);

    @Mapping(source = "entity.id.idDemande", target = "id.idDemande")
    @Mapping(source = "entity.id.idService", target = "id.idService")
    @Mapping(source = "entity.libelleService", target = "libelle")
    @Mapping(source = "entity.descriptionService", target = "description")
    @Mapping(source = "entity.prixUnitaireService", target = "prixUnitaire")
    DemandeServiceResponse toDto(DemandeService entity);

    @AfterMapping
    default void fillMissingSnapshots(DemandeService source, @MappingTarget DemandeServiceResponse target) {
        if (source == null || target == null) {
            return;
        }
        if (target.getLibelle() == null && source.getService() != null) {
            target.setLibelle(source.getService().getLibelle());
        }
        if (target.getDescription() == null && source.getService() != null) {
            target.setDescription(source.getService().getDescription());
        }
        if (target.getPrixUnitaire() == null && source.getService() != null) {
            target.setPrixUnitaire(source.getService().getPrixUnitaire());
        }
    }
}
