package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DemandeMapper {

    // ----------- ENTITY -> DTO -----------

    @Mapping(target = "client", source = "client") // utilisera toClientSummaryDto
    @Mapping(target = "typeDemande", source = "typeDemande")
    @Mapping(target = "statutDemande", source = "statutDemande")
    @Mapping(target = "services", source = "services") // utilisera toDemandeServiceDto
    DemandeResponse toResponse(Demande ent);

    // Client -> ClientSummaryDto
    @Mapping(target = "idClient", source = "idClient")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "prenom", source = "prenom")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "telephone", source = "telephone")
    @Mapping(target = "immatriculation", source = "immatriculation")
    @Mapping(target = "adresseLigne1", source = "adresseLigne1")
    @Mapping(target = "adresseLigne2", source = "adresseLigne2")
    @Mapping(target = "adresseCodePostal", source = "adresseCodePostal")
    @Mapping(target = "adresseVille", source = "adresseVille")
    ClientSummaryDto toClientSummaryDto(Client client);

    // DemandeService -> DemandeServiceDto
    @Mapping(target = "idService", source = "id.idService")
    @Mapping(target = "libelle", source = "libelleService")
    @Mapping(target = "description", source = "descriptionService")
    @Mapping(target = "prixUnitaire", source = "prixUnitaireService")
    @Mapping(target = "quantite", source = "quantite")
    DemandeServiceDto toDemandeServiceDto(DemandeService ds);

    @AfterMapping
    default void ensureServiceFallback(DemandeService source, @MappingTarget DemandeServiceDto target) {
        if (source == null || target == null) {
            return;
        }
        if (target.getQuantiteMax() == null && source.getService() != null) {
            target.setQuantiteMax(source.getService().getQuantiteMax());
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

    // TypeDemande -> TypeDemandeDto (si tu n’as pas déjà un mapper dédié)
    TypeDemandeDto toDto(TypeDemande td);

    // StatutDemande -> StatutDemandeDto
    StatutDemandeDto toDto(StatutDemande sd);

    // BONUS : si la demande est de type "RendezVous", on écrase le statutDemande par le statut du RDV
    @AfterMapping
    default void overrideStatutForRendezVous(Demande src, @MappingTarget DemandeResponse target) {
        if (src == null || src.getTypeDemande() == null) return;
        if (!"RendezVous".equals(src.getTypeDemande().getCodeType())) return;

        RendezVous rdv = src.getRendezVous(); // suppose un champ OneToOne<->Demande
        if (rdv != null && rdv.getStatut() != null) {
            // On mappe le StatutRendezVous → StatutDemandeDto (mêmes champs code/libellé)
            target.setStatutDemande(new StatutDemandeDto(
                    rdv.getStatut().getCodeStatut(),
                    rdv.getStatut().getLibelle()
            ));
        }
    }

    // ----------- DTO -> ENTITY (pour create/update) -----------
    // On ignore les relations ; on les associe en Service (par codes/ids)
    @Mapping(target = "idDemande", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "typeDemande", ignore = true)
    @Mapping(target = "statutDemande", ignore = true)
    @Mapping(target = "services", ignore = true)
    @Mapping(target = "rendezVous", ignore = true) // si lien 1-1
    Demande toEntity(DemandeRequest req);
}
