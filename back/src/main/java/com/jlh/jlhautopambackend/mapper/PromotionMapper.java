package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.Promotion;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Value;

@Mapper(componentModel = "spring")
public abstract class PromotionMapper {

    @Value("${app.images.base-url}")
    private String imagesBaseUrl;

    /**
     * Construction de l’entité depuis la requête.
     * On ne touche pas à l’ID ni à l’admin (gérés par le service).
     * Mais on doit mapper la description !
     */
    @Mapping(target = "idPromotion",    ignore = true)
    @Mapping(target = "administrateur", ignore = true)
    @Mapping(target = "description",    source = "dto.description")
    public abstract Promotion toEntity(PromotionRequest dto);

    /**
     * Création du DTO de base (sans imageUrl, qu’on construit à la main).
     * On ajoute ici le mapping de description.
     */
    @Mapping(target = "idPromotion",     source = "entity.idPromotion")
    @Mapping(target = "administrateurId",source = "entity.administrateur.idAdmin")
    @Mapping(target = "imageUrl",        ignore = true)
    @Mapping(target = "validFrom",      source = "entity.validFrom")
    @Mapping(target = "validTo",        source = "entity.validTo")
    @Mapping(target = "description",    source = "entity.description")
    protected abstract PromotionResponse toResponseBase(Promotion entity);

    /**
     * Méthode “manuelle” pour enrichir le DTO avec l’URL complète de l’image.
     * La description est déjà positionnée par toResponseBase().
     */
    public PromotionResponse toResponse(Promotion entity) {
        PromotionResponse response = toResponseBase(entity);

        // Calcul de l’URL de l’image
        String imagePath = entity.getImageUrl();
        if (imagePath == null || imagePath.isBlank()) {
            response.setImageUrl(null);
        } else if (imagePath.startsWith("http")) {
            response.setImageUrl(imagePath);
        } else {
            String normalized = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
            String base       = imagesBaseUrl.endsWith("/") ? imagesBaseUrl : imagesBaseUrl + "/";
            response.setImageUrl(base + normalized.replaceFirst("^promotions/images/", ""));
        }

        return response;
    }
}
