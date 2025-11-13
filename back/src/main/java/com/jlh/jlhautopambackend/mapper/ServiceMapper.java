package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.Service;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public class ServiceMapper {

    public ServiceResponse toResponse(Service s) {
        return ServiceResponse.builder()
                .idService(s.getIdService())
                .libelle(s.getLibelle())
                .description(s.getDescription())
                .prixUnitaire(s.getPrixUnitaire())
                .archived(s.isArchived())
                .build();
    }

    public Service toEntity(ServiceRequest req) {
        return Service.builder()
                .libelle(req.getLibelle())
                .description(req.getDescription())
                .prixUnitaire(req.getPrixUnitaire())
                .archived(false)
                .build();
    }
}