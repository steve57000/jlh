package com.jlh.jlhautopambackend.dto;

import com.jlh.jlhautopambackend.modeles.DemandeTimelineType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeTimelineRequest {

    @NotNull
    private DemandeTimelineType type;

    @Size(max = 1000)
    private String commentaire;

    @Size(max = 20)
    private String codeStatut;

    @DecimalMin(value = "0.0", inclusive = true, message = "Le montant doit être positif ou nul")
    private BigDecimal montantValide;

    private Boolean visibleClient;

    private Long documentId;
    private String documentNom;
}
