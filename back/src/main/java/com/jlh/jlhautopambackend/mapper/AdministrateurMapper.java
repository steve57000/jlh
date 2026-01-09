package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.AdministrateurRequest;
import com.jlh.jlhautopambackend.dto.AdministrateurResponse;
import com.jlh.jlhautopambackend.dto.DisponibiliteIdDto;
import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.modeles.Disponibilite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AdministrateurMapper {

    // On ignore l'ID côté création
    @Mapping(target = "idAdmin", ignore = true)
    // On ignore les disponibilités lors du toEntity
    @Mapping(target = "disponibilites", ignore = true)
    @Mapping(target = "niveauAcces",
            expression = "java(resolveNiveauAcces(dto.getNiveauAcces()))")
    Administrateur toEntity(AdministrateurRequest dto);

    // On convertit la liste de Disponibilite en liste de DisponibiliteIdDto
    @Mapping(target = "username",
            expression = "java(resolveUsername(entity))")
    @Mapping(target = "disponibilites",
            expression = "java(mapDisponibilites(entity.getDisponibilites()))")
    @Mapping(target = "niveauAcces",
            expression = "java(entity.getNiveauAcces() != null ? entity.getNiveauAcces().name() : null)")
    AdministrateurResponse toResponse(Administrateur entity);

    default List<DisponibiliteIdDto> mapDisponibilites(List<Disponibilite> src) {
        if (src == null) return null;
        return src.stream()
                .map(d -> new DisponibiliteIdDto(
                        d.getId().getIdAdmin(),
                        d.getId().getIdCreneau()))
                .collect(Collectors.toList());
    }

    default String resolveUsername(Administrateur entity) {
        if (entity == null) {
            return null;
        }
        String username = entity.getUsername();
        if (username != null && !username.isBlank()) {
            return username.trim();
        }
        String email = entity.getEmail();
        return (email != null && !email.isBlank()) ? email.trim() : null;
    }

    default com.jlh.jlhautopambackend.modeles.NiveauAccesAdministrateur resolveNiveauAcces(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return com.jlh.jlhautopambackend.modeles.NiveauAccesAdministrateur.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
