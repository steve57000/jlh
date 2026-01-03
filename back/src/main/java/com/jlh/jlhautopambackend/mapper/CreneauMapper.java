package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.CreneauRequest;
import com.jlh.jlhautopambackend.dto.CreneauResponse;
import com.jlh.jlhautopambackend.dto.DisponibiliteIdDto;
import com.jlh.jlhautopambackend.dto.StatutCreneauDto;
import com.jlh.jlhautopambackend.modeles.Creneau;
import com.jlh.jlhautopambackend.modeles.Disponibilite;
import com.jlh.jlhautopambackend.modeles.StatutCreneau;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CreneauMapper {

    // On ignore statut & dispos ici, on les gère dans le controller
    @Mapping(target = "idCreneau", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "disponibilites", ignore = true)
    Creneau toEntity(CreneauRequest dto);

    // Pour la réponse, on construit le DTO de statut et la liste d'IDs de dispo
    @Mapping(target = "statut", expression = "java(toDtoStatut(entity.getStatut()))")
    @Mapping(target = "disponibilites", expression = "java(mapDisponibilites(entity.getDisponibilites()))")
    CreneauResponse toResponse(Creneau entity);

    // Sérialise le statut en DTO
    default StatutCreneauDto toDtoStatut(StatutCreneau s) {
        if (s == null) return null;
        return StatutCreneauDto.builder()
                .codeStatut(s.getCodeStatut())
                .libelle(s.getLibelle())
                .build();
    }

    // Extrait les paires d'IDs de dispo sans boucles infinies
    default List<DisponibiliteIdDto> mapDisponibilites(List<Disponibilite> src) {
        if (src == null) return null;
        return src.stream()
                .map(d -> new DisponibiliteIdDto(
                        d.getId().getIdAdmin(),
                        d.getId().getIdCreneau()))
                .collect(Collectors.toList());
    }
}
