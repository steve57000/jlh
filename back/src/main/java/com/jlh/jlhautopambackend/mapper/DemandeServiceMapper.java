package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.DemandeServiceDto;
import com.jlh.jlhautopambackend.modeles.DemandeService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DemandeServiceMapper {

    @Mapping(target = "idDemande", source = "demande.idDemande")
    @Mapping(target = "idService", source = "service.idService")
    @Mapping(target = "libelleService", source = "service.libelle")
    @Mapping(target = "descriptionService", source = "service.description")
    @Mapping(target = "prixUnitaireService", source = "service.prixUnitaire")
    @Mapping(target = "quantiteMax", ignore = true)
    @Mapping(target = "privateNoteService", ignore = true)
    @Mapping(target = "dateHeureService", ignore = true)
    DemandeServiceDto toDto(DemandeService entity);

    List<DemandeServiceDto> toDtos(Set<DemandeService> services);
}
