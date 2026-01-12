package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.Service;
import com.jlh.jlhautopambackend.modeles.ServiceIcon;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public class ServiceMapper {

    public ServiceResponse toResponse(Service s) {
        ServiceIcon icon = s.getIcon();
        return ServiceResponse.builder()
                .idService(s.getIdService())
                .libelle(s.getLibelle())
                .description(s.getDescription())
                .descriptionLongue(s.getDescriptionLongue())
                .iconId(icon != null ? icon.getIdIcon() : null)
                .iconUrl(icon != null ? icon.getUrl() : null)
                .prixUnitaire(s.getPrixUnitaire())
                .quantiteMax(s.getQuantiteMax())
                .archived(s.isArchived())
                .build();
    }

    public Service toEntity(ServiceRequest req) {
        return Service.builder()
                .libelle(req.getLibelle())
                .description(req.getDescription())
                .descriptionLongue(req.getDescriptionLongue())
                .prixUnitaire(req.getPrixUnitaire())
                .quantiteMax(req.getQuantiteMax())
                .archived(false)
                .build();
    }
}
