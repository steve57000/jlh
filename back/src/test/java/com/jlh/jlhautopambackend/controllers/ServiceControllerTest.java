package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.dto.ServiceRequest;
import com.jlh.jlhautopambackend.dto.ServiceResponse;
import com.jlh.jlhautopambackend.repository.AdministrateurRepository;
import com.jlh.jlhautopambackend.services.RendezVousService;
import com.jlh.jlhautopambackend.services.support.AuthenticatedClientResolver;
import com.jlh.jlhautopambackend.services.ServiceService;
import com.jlh.jlhautopambackend.utils.JwtUtil;
import com.jlh.jlhautopambackend.config.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ServiceController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class ServiceControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ServiceService service;
    @MockitoBean private RendezVousService rendezVousService;
    @MockitoBean private AuthenticatedClientResolver clientResolver;
    @MockitoBean private AdministrateurRepository adminRepository;

    // mocks pour désactiver l'authent
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/services ➔ 200, JSON list")
    void testGetAll() throws Exception {
        ServiceResponse r1 = ServiceResponse.builder()
                .idService(1)
                .libelle("S1")
                .description("Desc1")
                .prixUnitaire(new BigDecimal("12.34"))
                .quantiteMax(5)
                .archived(false)
                .build();
        ServiceResponse r2 = ServiceResponse.builder()
                .idService(2)
                .libelle("S2")
                .description("Desc2")
                .prixUnitaire(new BigDecimal("56.78"))
                .quantiteMax(2)
                .archived(false)
                .build();

        Mockito.when(service.findAll()).thenReturn(Arrays.asList(r1, r2));

        mvc.perform(get("/api/services")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idService").value(1))
                .andExpect(jsonPath("$[0].archived").value(false))
                .andExpect(jsonPath("$[0].quantiteMax").value(5))
                .andExpect(jsonPath("$[1].libelle").value("S2"));
    }

    @Test
    @DisplayName("GET /api/services/{id} ➔ 200")
    void testGetByIdFound() throws Exception {
        ServiceResponse resp = ServiceResponse.builder()
                .idService(1)
                .libelle("S1")
                .description("Desc1")
                .prixUnitaire(new BigDecimal("12.34"))
                .quantiteMax(3)
                .archived(false)
                .build();
        Mockito.when(service.findById(1)).thenReturn(Optional.of(resp));

        mvc.perform(get("/api/services/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Desc1"))
                .andExpect(jsonPath("$.quantiteMax").value(3))
                .andExpect(jsonPath("$.archived").value(false));
    }

    @Test
    @DisplayName("GET /api/services/{id} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(service.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/services/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/services ➔ 201, retourne JSON + Location")
    void testCreate() throws Exception {
        ServiceRequest req = ServiceRequest.builder()
                .libelle("New")
                .description("NewDesc")
                .prixUnitaire(new BigDecimal("99.99"))
                .quantiteMax(7)
                .build();
        ServiceResponse saved = ServiceResponse.builder()
                .idService(3)
                .libelle("New")
                .description("NewDesc")
                .prixUnitaire(new BigDecimal("99.99"))
                .quantiteMax(7)
                .archived(false)
                .build();

        Mockito.when(service.create(Mockito.any(ServiceRequest.class)))
                .thenReturn(saved);

        mvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/services/3"))
                .andExpect(jsonPath("$.idService").value(3))
                .andExpect(jsonPath("$.prixUnitaire").value(99.99))
                .andExpect(jsonPath("$.quantiteMax").value(7))
                .andExpect(jsonPath("$.archived").value(false));
    }

    @Test
    @DisplayName("PUT /api/services/{id} ➔ 200, JSON mis à jour")
    void testUpdateFound() throws Exception {
        ServiceRequest updates = ServiceRequest.builder()
                .libelle("Updated")
                .description("UpdDesc")
                .prixUnitaire(new BigDecimal("20.00"))
                .quantiteMax(6)
                .build();
        ServiceResponse saved = ServiceResponse.builder()
                .idService(1)
                .libelle("Updated")
                .description("UpdDesc")
                .prixUnitaire(new BigDecimal("20.00"))
                .quantiteMax(6)
                .archived(false)
                .build();

        Mockito.when(service.update(Mockito.eq(1), Mockito.any(ServiceRequest.class)))
                .thenReturn(Optional.of(saved));

        mvc.perform(put("/api/services/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("Updated"))
                .andExpect(jsonPath("$.prixUnitaire").value(20.00))
                .andExpect(jsonPath("$.quantiteMax").value(6))
                .andExpect(jsonPath("$.archived").value(false));
    }

    @Test
    @DisplayName("PUT /api/services/{id} ➔ 404")
    void testUpdateNotFound() throws Exception {
        ServiceRequest updates = ServiceRequest.builder()
                .libelle("X")
                .description("X")
                .prixUnitaire(new BigDecimal("1.00"))
                .quantiteMax(1)
                .build();
        Mockito.when(service.update(Mockito.eq(99), Mockito.any(ServiceRequest.class)))
                .thenReturn(Optional.empty());

        mvc.perform(put("/api/services/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/services/{id} ➔ 204")
    void testDeleteFound() throws Exception {
        Mockito.when(service.delete(1)).thenReturn(true);

        mvc.perform(delete("/api/services/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/services/{id} ➔ 404")
    void testDeleteNotFound() throws Exception {
        Mockito.when(service.delete(99)).thenReturn(false);

        mvc.perform(delete("/api/services/99"))
                .andExpect(status().isNotFound());
    }
}
