package com.jlh.jlhautopambackend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal prixUnitaire;
}