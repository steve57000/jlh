package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.DisponibiliteRequest;
import com.jlh.jlhautopambackend.dto.DisponibiliteResponse;
import com.jlh.jlhautopambackend.dto.DisponibiliteIdDto;
import com.jlh.jlhautopambackend.modeles.Disponibilite;
import com.jlh.jlhautopambackend.modeles.DisponibiliteKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisponibiliteMapperTest {

    private final DisponibiliteMapper mapper = DisponibiliteMapper.INSTANCE;

    @Test
    void toEntity_shouldReturnNull_whenInputIsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toEntity_shouldMapDtoToEntity() {
        // given
        int adminId = 5;
        int creneauId = 42;
        DisponibiliteRequest dto = new DisponibiliteRequest(adminId, creneauId);

        // when
        Disponibilite ent = mapper.toEntity(dto);

        // then
        assertNotNull(ent, "L'entité ne doit pas être null");
        assertNotNull(ent.getId(), "La clé composite ne doit pas être null");
        assertEquals(adminId, ent.getId().getIdAdmin(), "L'idAdmin doit être mappé");
        assertEquals(creneauId, ent.getId().getIdCreneau(), "L'idCreneau doit être mappé");
    }

    @Test
    void toResponse_shouldReturnNull_whenInputIsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toResponse_shouldMapEntityToDto() {
        // given
        int adminId = 7;
        int creneauId = 99;
        DisponibiliteKey key = new DisponibiliteKey(adminId, creneauId);
        Disponibilite ent = Disponibilite.builder()
                .id(key)
                .build();

        // when
        DisponibiliteResponse resp = mapper.toResponse(ent);

        // then
        assertNotNull(resp, "Le DTO ne doit pas être null");
        DisponibiliteIdDto idDto = resp.getId();
        assertNotNull(idDto, "L'objet id ne doit pas être null");
        assertEquals(adminId, idDto.getIdAdmin(), "L'idAdmin doit être mappé");
        assertEquals(creneauId, idDto.getIdCreneau(), "L'idCreneau doit être mappé");
    }
}
