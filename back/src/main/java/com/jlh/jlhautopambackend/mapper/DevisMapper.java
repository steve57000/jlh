package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.DevisRequest;
import com.jlh.jlhautopambackend.dto.DevisResponse;
import com.jlh.jlhautopambackend.modeles.Devis;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DevisMapper {

    @Mapping(target = "idDevis", ignore = true)
    @Mapping(target = "demande", ignore = true)
    @Mapping(target = "rendezVousId", source = "dto.rendezVousId")
    Devis toEntity(DevisRequest dto);
    // montantTotal, montantMainOeuvre, montantPieces, dateDevis
    // seront mappés automatiquement par nom si présents dans DevisRequest

    @Mapping(target = "idDevis", source = "entity.idDevis")
    @Mapping(target = "demandeId", source = "entity.demande.idDemande")
    @Mapping(target = "dateDevis", source = "entity.dateDevis")
    @Mapping(target = "montantTotal", source = "entity.montantTotal")
    @Mapping(target = "montantMainOeuvre", source = "entity.montantMainOeuvre")
    @Mapping(target = "montantPieces", source = "entity.montantPieces")
    @Mapping(target = "rendezVousId", source = "entity.rendezVousId")
    DevisResponse toResponse(Devis entity);
}
