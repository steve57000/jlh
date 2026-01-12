package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.*;
import com.jlh.jlhautopambackend.services.UserService;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {DemandeTimelineMapper.class, ClientMapper.class},
        builder = @Builder(disableBuilder = true),
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface DemandeMapper {

    // ----------- ENTITY -> RESPONSE DTO -----------

    @Mapping(target = "client", source = "client")
    @Mapping(target = "typeDemande", source = "typeDemande")
    @Mapping(target = "statutDemande", source = "statutDemande")
    @Mapping(target = "services", source = "services")
    @Mapping(target = "documents", source = "documents")
    @Mapping(target = "timeline", source = "timelineEntries")
    @Mapping(target = "devis", ignore = true)
    @Mapping(target = "rendezVous", ignore = true)
    DemandeResponse toResponse(
            Demande ent,
            @Context UserService userService
    );

    // ----------- ENTITY -> LIST DTO -----------

    @Mapping(target = "dateSoumission", source = "dateDemande")
    @Mapping(target = "codeType", source = "typeDemande.codeType")
    @Mapping(target = "typeLibelle", source = "typeDemande.libelle")
    @Mapping(target = "codeStatut", source = "statutDemande.codeStatut")
    @Mapping(target = "statutLibelle", source = "statutDemande.libelle")
    @Mapping(target = "client", source = "client")
    @Mapping(target = "services", source = "services")
    DemandeDto toDto(Demande ent);

    List<DemandeDto> toDtos(List<Demande> entities);

    // ----------- CLIENT -> DTO -----------

    @Mapping(target = "idClient", source = "idClient")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "prenom", source = "prenom")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "telephone", source = "telephone")
    @Mapping(target = "immatriculation", source = "immatriculation")
    @Mapping(target = "vehiculeMarque", source = "vehiculeMarque")
    @Mapping(target = "vehiculeModele", source = "vehiculeModele")
    @Mapping(target = "vehiculeEnergie", source = "vehiculeEnergie")
    @Mapping(target = "adresseLigne1", source = "adresseLigne1")
    @Mapping(target = "adresseLigne2", source = "adresseLigne2")
    @Mapping(target = "adresseCodePostal", source = "adresseCodePostal")
    @Mapping(target = "adresseVille", source = "adresseVille")
    ClientSummaryDto toClientSummaryDto(Client client);

    // ----------- SERVICE -> DTO -----------

    @Mapping(target = "idDemande", source = "demande.idDemande")
    @Mapping(target = "idService", source = "id.idService")
    @Mapping(target = "libelle", source = "libelleService")
    @Mapping(target = "libelleService", source = "libelleService")
    @Mapping(target = "description", source = "descriptionService")
    @Mapping(target = "descriptionService", source = "descriptionService")
    @Mapping(target = "prixUnitaire", source = "prixUnitaireService")
    @Mapping(target = "prixUnitaireService", source = "prixUnitaireService")
    @Mapping(target = "quantite", source = "quantite")
    @Mapping(target = "quantiteMax", source = "service.quantiteMax")
    @Mapping(target = "privateNoteService", ignore = true)
    @Mapping(target = "dateHeureService", ignore = true)
    @Mapping(target = "rendezVousId", source = "rendezVousId")
    DemandeServiceDto toDemandeServiceDto(DemandeService ds);

    @AfterMapping
    default void ensureServiceFallback(DemandeService source, @MappingTarget DemandeServiceDto target) {
        if (source == null || target == null) return;

        if (target.getIdService() == null && source.getService() != null)
            target.setIdService(source.getService().getIdService());

        if (target.getIdService() == null && source.getId() != null)
            target.setIdService(source.getId().getIdService());

        if (target.getIdDemande() == null && source.getDemande() != null)
            target.setIdDemande(source.getDemande().getIdDemande());

        if (target.getIdDemande() == null && source.getId() != null)
            target.setIdDemande(source.getId().getIdDemande());

        if (target.getQuantiteMax() == null && source.getService() != null)
            target.setQuantiteMax(source.getService().getQuantiteMax());
    }

    // ----------- TYPE / STATUT -----------

    TypeDemandeDto toDto(TypeDemande td);

    StatutDemandeDto toDto(StatutDemande sd);

    // ----------- OVERRIDE STATUT POUR RDV -----------

    @AfterMapping
    default void overrideStatutForRendezVous(Demande src, @MappingTarget DemandeResponse target) {
        if (src == null || src.getTypeDemande() == null) return;
        if (!"RendezVous".equals(src.getTypeDemande().getCodeType())) return;

        RendezVous rdv = src.getRendezVous();
        if (rdv != null && rdv.getStatut() != null) {
            target.setStatutDemande(new StatutDemandeDto(
                    rdv.getStatut().getCodeStatut(),
                    rdv.getStatut().getLibelle()
            ));
        }
    }

    // ----------- DTO -> ENTITY -----------

    @Mapping(target = "idDemande", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "typeDemande", ignore = true)
    @Mapping(target = "statutDemande", ignore = true)
    @Mapping(target = "services", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "rendezVous", ignore = true)
    @Mapping(target = "devis", ignore = true)
    @Mapping(target = "timelineEntries", ignore = true)
    Demande toEntity(DemandeRequest req);
}
