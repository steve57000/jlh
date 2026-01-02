package com.jlh.jlhautopambackend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeServiceDto {
    private Integer idDemande;
    private Integer idService;
    private String libelle;
    private String libelleService;
    private String description;
    private String descriptionService;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal prixUnitaireService;
    private Integer quantiteMax;
    private String privateNoteService;
    private Instant dateHeureService;

    public String getLibelle() {
        return libelle != null ? libelle : libelleService;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
        if (this.libelleService == null) {
            this.libelleService = libelle;
        }
    }

    public String getLibelleService() {
        return libelleService != null ? libelleService : libelle;
    }

    public void setLibelleService(String libelleService) {
        this.libelleService = libelleService;
        this.libelle = libelleService;
    }

    public String getDescription() {
        return description != null ? description : descriptionService;
    }

    public void setDescription(String description) {
        this.description = description;
        if (this.descriptionService == null) {
            this.descriptionService = description;
        }
    }

    public String getDescriptionService() {
        return descriptionService != null ? descriptionService : description;
    }

    public void setDescriptionService(String descriptionService) {
        this.descriptionService = descriptionService;
        this.description = descriptionService;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire != null ? prixUnitaire : prixUnitaireService;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
        if (this.prixUnitaireService == null) {
            this.prixUnitaireService = prixUnitaire;
        }
    }

    public BigDecimal getPrixUnitaireService() {
        return prixUnitaireService != null ? prixUnitaireService : prixUnitaire;
    }

    public void setPrixUnitaireService(BigDecimal prixUnitaireService) {
        this.prixUnitaireService = prixUnitaireService;
        this.prixUnitaire = prixUnitaireService;
    }
}
