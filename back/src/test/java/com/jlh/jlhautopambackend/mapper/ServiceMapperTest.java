package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.ServiceRequest;
import com.jlh.jlhautopambackend.dto.ServiceResponse;
import com.jlh.jlhautopambackend.modeles.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ServiceMapperTest {

    private ServiceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ServiceMapper();
    }

    @Test
    void toResponse_mapsEntityToDto() {
        Service entity = Service.builder()
                .idService(42)
                .libelle("Free wash")
                .description("Lavage intérieur et extérieur")
                .prixUnitaire(new BigDecimal("19.90"))
                .quantiteMax(3)
                .archived(true)
                .build();

        ServiceResponse dto = mapper.toResponse(entity);

        assertNotNull(dto);
        assertEquals(42, dto.getIdService());
        assertEquals("Free wash", dto.getLibelle());
        assertEquals("Lavage intérieur et extérieur", dto.getDescription());
        assertEquals(new BigDecimal("19.90"), dto.getPrixUnitaire());
        assertEquals(3, dto.getQuantiteMax());
        assertTrue(dto.isArchived());
    }

    @Test
    void toEntity_mapsRequestToEntity() {
        ServiceRequest req = ServiceRequest.builder()
                .libelle("Premium wash")
                .description("Lavage complet + cire")
                .prixUnitaire(new BigDecimal("29.90"))
                .quantiteMax(8)
                .build();

        Service entity = mapper.toEntity(req);

        assertNotNull(entity);
        // l'id reste null car non mappé
        assertNull(entity.getIdService());
        assertEquals("Premium wash", entity.getLibelle());
        assertEquals("Lavage complet + cire", entity.getDescription());
        assertEquals(new BigDecimal("29.90"), entity.getPrixUnitaire());
        assertEquals(8, entity.getQuantiteMax());
        assertFalse(entity.isArchived(), "le mapper doit initialiser archived à false");
    }
}
