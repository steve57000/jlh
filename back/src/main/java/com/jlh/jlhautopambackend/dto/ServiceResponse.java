package com.jlh.jlhautopambackend.dto;

import lombok.*;

import com.jlh.jlhautopambackend.modeles.ServicePrixMode;
import com.jlh.jlhautopambackend.modeles.ServiceQuantiteMode;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceResponse {
    private Integer idService;
    private String libelle;
    private String description;
    private String descriptionLongue;
    private Integer iconId;
    private String iconUrl;
    private BigDecimal prixUnitaire;
    private ServiceQuantiteMode quantiteMode;
    private ServicePrixMode prixMode;
    private Integer tailleLot;
    private boolean archived;
    private Integer quantiteMax;
}
