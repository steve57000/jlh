package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.StatutRendezVousDto;
import com.jlh.jlhautopambackend.modeles.StatutRendezVous;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public class StatutRendezVousMapper {

    public StatutRendezVousDto toDto(StatutRendezVous e) {
        return StatutRendezVousDto.builder()
                .codeStatut(e.getCodeStatut())
                .libelle(e.getLibelle())
                .build();
    }

    public StatutRendezVous toEntity(StatutRendezVousDto d) {
        return StatutRendezVous.builder()
                .codeStatut(d.getCodeStatut())
                .libelle(d.getLibelle())
                .build();
    }

    public void updateEntity(StatutRendezVousDto d, StatutRendezVous e) {
        e.setLibelle(d.getLibelle());
    }
}
