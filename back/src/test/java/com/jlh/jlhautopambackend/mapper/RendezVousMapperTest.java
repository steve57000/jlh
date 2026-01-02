package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.RendezVousRequest;
import com.jlh.jlhautopambackend.dto.RendezVousResponse;
import com.jlh.jlhautopambackend.dto.StatutRendezVousDto;
import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.modeles.Creneau;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.RendezVous;
import com.jlh.jlhautopambackend.modeles.StatutRendezVous;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class RendezVousMapperTest {

    private static RendezVousMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = Mappers.getMapper(RendezVousMapper.class);
    }

    @Test
    @DisplayName("toEntity : mapping minimal de RendezVousRequest vers RendezVous")
    void testToEntity() {
        // given
        RendezVousRequest dto = RendezVousRequest.builder()
                .demandeId(11)
                .creneauId(22)
                .administrateurId(33)
                .codeStatut("STAT")
                .build();

        // when
        RendezVous entity = mapper.toEntity(dto);

        // then
        // les champs ignorés doivent être null
        assertThat(entity.getIdRdv()).isNull();
        assertThat(entity.getDemande()).isNull();
        assertThat(entity.getCreneau()).isNull();
        assertThat(entity.getAdministrateur()).isNull();
        assertThat(entity.getStatut()).isNull();
        // le mapper n'affecte pas de dateRdv car non défini dans DTO
    }

    @Test
    @DisplayName("toResponse : mapping complet de RendezVous vers RendezVousResponse")
    void testToResponse() {
        // given
        Demande demande = Demande.builder().idDemande(11).build();
        Creneau creneau = Creneau.builder().idCreneau(22).build();
        Administrateur admin = Administrateur.builder()
                .idAdmin(33)
                .username("admin33")
                .build();
        StatutRendezVous statut = StatutRendezVous.builder()
                .codeStatut("OK")
                .libelle("Validé")
                .build();

        RendezVous entity = RendezVous.builder()
                .idRdv(55)
                .demande(demande)
                .creneau(creneau)
                .administrateur(admin)
                .statut(statut)
                .build();

        // when
        RendezVousResponse resp = mapper.toResponse(entity);

        // then
        assertThat(resp).isNotNull();
        assertThat(resp.getIdRdv()).isEqualTo(55);
        assertThat(resp.getDemandeId()).isEqualTo(11);
        assertThat(resp.getCreneauId()).isEqualTo(22);
        assertThat(resp.getAdministrateurId()).isEqualTo(33);

        StatutRendezVousDto dtoStatut = resp.getStatut();
        assertThat(dtoStatut).isNotNull();
        assertThat(dtoStatut.getCodeStatut()).isEqualTo("OK");
        assertThat(dtoStatut.getLibelle()).isEqualTo("Validé");
    }
}
