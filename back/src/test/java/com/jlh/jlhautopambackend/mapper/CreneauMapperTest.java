package com.jlh.jlhautopambackend.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.jlh.jlhautopambackend.dto.CreneauRequest;
import com.jlh.jlhautopambackend.dto.CreneauResponse;
import com.jlh.jlhautopambackend.dto.DisponibiliteIdDto;
import com.jlh.jlhautopambackend.dto.StatutCreneauDto;
import com.jlh.jlhautopambackend.modeles.Creneau;
import com.jlh.jlhautopambackend.modeles.Disponibilite;
import com.jlh.jlhautopambackend.modeles.DisponibiliteKey;
import com.jlh.jlhautopambackend.modeles.StatutCreneau;

class CreneauMapperTest {

    private final CreneauMapper mapper = Mappers.getMapper(CreneauMapper.class);

    @Test
    void toEntity_shouldIgnoreIdStatutAndDisponibilites() {
        Instant debut = Instant.parse("2025-01-01T10:00:00Z");
        Instant fin   = Instant.parse("2025-01-01T11:00:00Z");

        CreneauRequest req = CreneauRequest.builder()
                .dateDebut(debut)
                .dateFin(fin)
                .codeStatut("OK")      // sera ignoré
                .build();

        Creneau ent = mapper.toEntity(req);

        assertNull(ent.getIdCreneau());
        assertEquals(debut, ent.getDateDebut());
        assertEquals(fin, ent.getDateFin());
        assertNull(ent.getStatut());
        assertNull(ent.getDisponibilites());
    }

    @Test
    void toResponse_shouldMapStatutAndDisponibilitesIds() {
        Instant debut = Instant.parse("2025-02-01T09:00:00Z");
        Instant fin   = Instant.parse("2025-02-01T10:00:00Z");

        StatutCreneau statut = StatutCreneau.builder()
                .codeStatut("VAL")
                .libelle("Validé")
                .build();

        Disponibilite d1 = Disponibilite.builder()
                .id(new DisponibiliteKey(7, 70))
                .build();
        Disponibilite d2 = Disponibilite.builder()
                .id(new DisponibiliteKey(8, 80))
                .build();

        Creneau ent = Creneau.builder()
                .idCreneau(5)
                .dateDebut(debut)
                .dateFin(fin)
                .statut(statut)
                .disponibilites(List.of(d1, d2))
                .build();

        CreneauResponse resp = mapper.toResponse(ent);

        assertEquals(5, resp.getIdCreneau());
        assertEquals(debut, resp.getDateDebut());
        assertEquals(fin, resp.getDateFin());

        StatutCreneauDto sdto = resp.getStatut();
        assertNotNull(sdto);
        assertEquals("VAL", sdto.getCodeStatut());
        assertEquals("Validé", sdto.getLibelle());

        List<DisponibiliteIdDto> l = resp.getDisponibilites();
        assertEquals(2, l.size());
        // vérifier que les clés ont bien été extraites
        assertTrue(l.stream().anyMatch(x -> x.getIdAdmin() == 7 && x.getIdCreneau() == 70));
        assertTrue(l.stream().anyMatch(x -> x.getIdAdmin() == 8 && x.getIdCreneau() == 80));
    }
}
