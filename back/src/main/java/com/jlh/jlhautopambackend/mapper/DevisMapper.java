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
    Devis toEntity(DevisRequest dto);

    @Mapping(target = "idDevis", source = "entity.idDevis")
    @Mapping(target = "demandeId", source = "entity.demande.idDemande")
    @Mapping(target = "dateDevis", source = "entity.dateDevis")
    @Mapping(target = "montantTotal", source = "entity.montantTotal")
    DevisResponse toResponse(Devis entity);
}
