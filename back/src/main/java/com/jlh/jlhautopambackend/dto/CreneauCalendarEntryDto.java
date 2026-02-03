package com.jlh.jlhautopambackend.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreneauCalendarEntryDto {
    private Integer idCreneau;
    private Instant dateDebut;
    private Instant dateFin;
    private String codeStatut;
    private String libelleStatut;
    private Integer totalCount;
    private Integer availableCount;
    private Integer reservedCount;
    private Integer unavailableCount;
}
