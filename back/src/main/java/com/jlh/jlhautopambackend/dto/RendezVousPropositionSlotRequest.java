package com.jlh.jlhautopambackend.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RendezVousPropositionSlotRequest {
    @NotNull
    private Instant dateDebut;

    @NotNull
    private Instant dateFin;
}
