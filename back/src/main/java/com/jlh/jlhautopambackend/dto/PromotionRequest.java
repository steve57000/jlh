package com.jlh.jlhautopambackend.dto;

import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionRequest {
    /** Identifiant de l’administrateur porteur de la promo */
    private Integer administrateurId;
    /** URL ou chemin vers l’image */
    private String imageUrl;
    /** Date de début de validité */
    private Instant validFrom;
    /** Date de fin de validité */
    private Instant validTo;
    /** Description de la promotion */
    private String description;
}