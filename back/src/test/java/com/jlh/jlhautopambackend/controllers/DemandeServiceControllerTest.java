package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.config.JwtAuthenticationFilter;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import com.jlh.jlhautopambackend.services.DemandeServiceService;
import com.jlh.jlhautopambackend.services.support.AuthenticatedClientResolver;
import com.jlh.jlhautopambackend.utils.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = DemandeServiceController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class DemandeServiceControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean DemandeServiceService service;
    @MockitoBean DemandeRepository demandeRepository;
    @MockitoBean AuthenticatedClientResolver clientResolver;
    @MockitoBean JwtUtil jwtUtil;
    @MockitoBean JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test @DisplayName("GET /api/demandes-services ➔ 200, json list")
    void testGetAll() throws Exception {
        DemandeServiceKeyDto k1 = new DemandeServiceKeyDto(1,10);
        DemandeServiceKeyDto k2 = new DemandeServiceKeyDto(2,20);
        DemandeServiceResponse r1 = DemandeServiceResponse.builder()
                .id(k1)
                .quantite(2)
                .libelle("Service 10")
                .description("desc")
                .prixUnitaire(BigDecimal.valueOf(150))
                .build();
        DemandeServiceResponse r2 = DemandeServiceResponse.builder()
                .id(k2)
                .quantite(5)
                .libelle("Service 20")
                .description("desc")
                .prixUnitaire(BigDecimal.valueOf(200))
                .build();
        Mockito.when(service.findAll()).thenReturn(List.of(r1,r2));

        mvc.perform(get("/api/demandes-services").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id.idDemande").value(1))
                .andExpect(jsonPath("$[1].id.idService").value(20));
    }

    @Test @DisplayName("GET by key ➔ 200")
    void testGetByKeyFound() throws Exception {
        DemandeServiceKeyDto key = new DemandeServiceKeyDto(3,30);
        DemandeServiceResponse resp = DemandeServiceResponse.builder()
                .id(key)
                .quantite(7)
                .libelle("Service 30")
                .description("desc")
                .prixUnitaire(BigDecimal.valueOf(175))
                .build();
        Mockito.when(service.findByKey(3,30)).thenReturn(Optional.of(resp));

        mvc.perform(get("/api/demandes-services/3/30").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantite").value(7));
    }

    @Test @DisplayName("GET by key ➔ 404")
    void testGetByKeyNotFound() throws Exception {
        Mockito.when(service.findByKey(9,90)).thenReturn(Optional.empty());
        mvc.perform(get("/api/demandes-services/9/90"))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("POST ➔ 201")
    void testCreate() throws Exception {
        DemandeServiceRequest req = new DemandeServiceRequest(5,50,3);
        DemandeServiceKeyDto key = new DemandeServiceKeyDto(5,50);
        DemandeServiceResponse created = DemandeServiceResponse.builder()
                .id(key)
                .quantite(3)
                .libelle("Service 50")
                .description("desc")
                .prixUnitaire(BigDecimal.valueOf(190))
                .build();
        Mockito.when(service.create(Mockito.any())).thenReturn(created);

        mvc.perform(post("/api/demandes-services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location","/api/demandes-services/5/50"))
                .andExpect(jsonPath("$.id.idDemande").value(5));
    }

    @Test @DisplayName("PUT ➔ 200")
    void testUpdate() throws Exception {
        DemandeServiceRequest req = new DemandeServiceRequest(null,null,9);
        DemandeServiceKeyDto key = new DemandeServiceKeyDto(7,70);
        DemandeServiceResponse updated = DemandeServiceResponse.builder()
                .id(key)
                .quantite(9)
                .libelle("Service 70")
                .description("desc")
                .prixUnitaire(BigDecimal.valueOf(210))
                .build();
        Mockito.when(service.update(Mockito.eq(7),Mockito.eq(70),Mockito.any()))
                .thenReturn(Optional.of(updated));

        mvc.perform(put("/api/demandes-services/7/70")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantite").value(9));
    }

    @Test @DisplayName("PUT ➔ 404")
    void testUpdateNotFound() throws Exception {
        Mockito.when(service.update(Mockito.eq(8),Mockito.eq(80),Mockito.any()))
                .thenReturn(Optional.empty());

        mvc.perform(put("/api/demandes-services/8/80")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("DELETE ➔ 204")
    void testDeleteFound() throws Exception {
        Mockito.when(service.delete(11,110)).thenReturn(true);
        mvc.perform(delete("/api/demandes-services/11/110"))
                .andExpect(status().isNoContent());
    }

    @Test @DisplayName("DELETE ➔ 404")
    void testDeleteNotFound() throws Exception {
        Mockito.when(service.delete(12,120)).thenReturn(false);
        mvc.perform(delete("/api/demandes-services/12/120"))
                .andExpect(status().isNotFound());
    }
}
