package com.jlh.jlhautopambackend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

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

    private String icon;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal prixUnitaire;

    @NotNull
    @Min(1)
    private Integer quantiteMax;
}
