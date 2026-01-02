package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.DemandeTimeline;
import com.jlh.jlhautopambackend.modeles.DemandeTimelineType;
import com.jlh.jlhautopambackend.services.UserService;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DemandeTimelineMapper {

    @Mapping(target = "createdBy", expression = "java(userService.getFirstnameFromEmail(entity.getCreatedBy()))")
    @Mapping(target = "statut", expression = "java(toStatutDto(entity))")
    @Mapping(target = "document", expression = "java(toDocumentDto(entity))")
    @Mapping(target = "rendezVous", expression = "java(toRendezVousDto(entity))")
    @Mapping(target = "source", constant = "TIMELINE")
    DemandeTimelineEntryDto toDto(DemandeTimeline entity, @Context UserService userService);

    List<DemandeTimelineEntryDto> toDtos(List<DemandeTimeline> entities, @Context UserService userService);

    default StatutDemandeDto toStatutDto(DemandeTimeline entity) {
        if (entity == null || entity.getStatutCode() == null) return null;
        return new StatutDemandeDto(entity.getStatutCode(), entity.getStatutLibelle());
    }

    default DemandeDocumentDto toDocumentDto(DemandeTimeline entity) {
        if (entity == null || entity.getDocumentId() == null) return null;
        return DemandeDocumentDto.builder()
                .idDocument(entity.getDocumentId())
                .nomFichier(entity.getDocumentNom())
                .visibleClient(entity.isVisibleClient())
                .build();
    }

    default RendezVousTimelineDto toRendezVousDto(DemandeTimeline entity) {
        if (entity == null || entity.getRendezVousId() == null) return null;
        return RendezVousTimelineDto.builder()
                .idRdv(entity.getRendezVousId())
                .codeStatut(entity.getRendezVousStatutCode())
                .libelleStatut(entity.getRendezVousStatutLibelle())
                .dateDebut(entity.getRendezVousDateDebut())
                .dateFin(entity.getRendezVousDateFin())
                .build();
    }
}
