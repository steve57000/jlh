package com.jlh.jlhautopambackend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import com.jlh.jlhautopambackend.modeles.ServicePrixMode;
import com.jlh.jlhautopambackend.modeles.ServiceQuantiteMode;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequest {
    @NotBlank
    @Size(max = 100)
    private String libelle;

    private String description;

    private String descriptionLongue;

    private Integer iconId;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal prixUnitaire;

    private ServiceQuantiteMode quantiteMode;

    private ServicePrixMode prixMode;

    @Min(1)
    private Integer tailleLot;

    @NotNull
    @Min(1)
    private Integer quantiteMax;
}
