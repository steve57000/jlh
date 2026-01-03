package com.jlh.jlhautopambackend.dto;

import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionResponse {
    /** Identifiant de la promotion */
    private Integer idPromotion;
    /** Identifiant de l’administrateur */
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