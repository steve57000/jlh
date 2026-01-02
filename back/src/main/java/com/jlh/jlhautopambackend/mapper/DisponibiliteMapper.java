package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.DisponibiliteRequest;
import com.jlh.jlhautopambackend.dto.DisponibiliteResponse;
import com.jlh.jlhautopambackend.dto.DisponibiliteIdDto;
import com.jlh.jlhautopambackend.modeles.Disponibilite;
import com.jlh.jlhautopambackend.modeles.DisponibiliteKey;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DisponibiliteMapper {
    DisponibiliteMapper INSTANCE = Mappers.getMapper(DisponibiliteMapper.class);

    default Disponibilite toEntity(DisponibiliteRequest dto) {
        if (dto == null) return null;
        return Disponibilite.builder()
                .id(new DisponibiliteKey(dto.getIdAdmin(), dto.getIdCreneau()))
                .build();
    }

    default DisponibiliteResponse toResponse(Disponibilite ent) {
        if (ent == null) return null;
        DisponibiliteKey key = ent.getId();
        DisponibiliteIdDto idDto = DisponibiliteIdDto.builder()
                .idAdmin(key.getIdAdmin())
                .idCreneau(key.getIdCreneau())
                .build();
        return DisponibiliteResponse.builder()
                .id(idDto)
                .build();
    }
}