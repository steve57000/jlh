package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.dto.CreneauRequest;
import com.jlh.jlhautopambackend.dto.CreneauResponse;
import com.jlh.jlhautopambackend.dto.StatutCreneauDto;
import com.jlh.jlhautopambackend.services.CreneauService;
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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CreneauController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class CreneauControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreneauService service;

    @MockitoBean
    private com.jlh.jlhautopambackend.utils.JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/creneaux ➔ 200, JSON list")
    void testGetAll() throws Exception {
        StatutCreneauDto statut = StatutCreneauDto.builder()
                .codeStatut("OK").libelle("OK").build();

        CreneauResponse r1 = CreneauResponse.builder()
                .idCreneau(1)
                .dateDebut(Instant.parse("2025-01-01T10:00:00Z"))
                .dateFin(Instant.parse("2025-01-01T11:00:00Z"))
                .statut(statut)
                .disponibilites(Collections.emptyList())
                .build();
        CreneauResponse r2 = CreneauResponse.builder()
                .idCreneau(2)
                .dateDebut(Instant.parse("2025-01-02T10:00:00Z"))
                .dateFin(Instant.parse("2025-01-02T11:00:00Z"))
                .statut(statut)
                .disponibilites(Collections.emptyList())
                .build();

        Mockito.when(service.findAll()).thenReturn(List.of(r1, r2));

        mvc.perform(get("/api/creneaux").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCreneau").value(1))
                .andExpect(jsonPath("$[1].idCreneau").value(2));
    }

    @Test
    @DisplayName("GET /api/creneaux/{id} ➔ 200")
    void testGetByIdFound() throws Exception {
        StatutCreneauDto statut = StatutCreneauDto.builder()
                .codeStatut("OK").libelle("OK").build();
        CreneauResponse resp = CreneauResponse.builder()
                .idCreneau(1)
                .dateDebut(Instant.parse("2025-01-01T10:00:00Z"))
                .dateFin(Instant.parse("2025-01-01T11:00:00Z"))
                .statut(statut)
                .disponibilites(Collections.emptyList())
                .build();

        Mockito.when(service.findById(1)).thenReturn(Optional.of(resp));

        mvc.perform(get("/api/creneaux/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCreneau").value(1));
    }

    @Test
    @DisplayName("GET /api/creneaux/{id} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(service.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/creneaux/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/creneaux ➔ 201, JSON retourné")
    void testCreate() throws Exception {
        CreneauRequest req = CreneauRequest.builder()
                .dateDebut(Instant.parse("2025-01-03T10:00:00Z"))
                .dateFin(Instant.parse("2025-01-03T11:00:00Z"))
                .codeStatut("NEW")
                .build();
        CreneauResponse created = CreneauResponse.builder()
                .idCreneau(5)
                .dateDebut(req.getDateDebut())
                .dateFin(req.getDateFin())
                .statut(StatutCreneauDto.builder().codeStatut("NEW").libelle("Nouveau").build())
                .disponibilites(Collections.emptyList())
                .build();

        Mockito.when(service.create(Mockito.any(CreneauRequest.class))).thenReturn(created);

        mvc.perform(post("/api/creneaux")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/creneaux/5"))
                .andExpect(jsonPath("$.idCreneau").value(5))
                .andExpect(jsonPath("$.statut.codeStatut").value("NEW"));
    }

    @Test
    @DisplayName("PUT /api/creneaux/{id} ➔ 200, JSON mis à jour")
    void testUpdateFound() throws Exception {
        CreneauRequest req = CreneauRequest.builder()
                .dateDebut(Instant.parse("2025-02-01T10:00:00Z"))
                .dateFin(Instant.parse("2025-02-01T11:00:00Z"))
                .codeStatut("NEW")
                .build();
        CreneauResponse updated = CreneauResponse.builder()
                .idCreneau(10)
                .dateDebut(req.getDateDebut())
                .dateFin(req.getDateFin())
                .statut(StatutCreneauDto.builder().codeStatut("NEW").libelle("New").build())
                .disponibilites(Collections.emptyList())
                .build();

        Mockito.when(service.update(Mockito.eq(10), Mockito.any(CreneauRequest.class)))
                .thenReturn(Optional.of(updated));

        mvc.perform(put("/api/creneaux/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut.codeStatut").value("NEW"));
    }

    @Test
    @DisplayName("PUT /api/creneaux/{id} ➔ 404")
    void testUpdateNotFound() throws Exception {
        CreneauRequest req = CreneauRequest.builder()
                .dateDebut(Instant.parse("2025-03-01T10:00:00Z"))
                .dateFin(Instant.parse("2025-03-01T11:00:00Z"))
                .codeStatut("OK")
                .build();
        Mockito.when(service.update(Mockito.eq(99), Mockito.any(CreneauRequest.class)))
                .thenReturn(Optional.empty());

        mvc.perform(put("/api/creneaux/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/creneaux/{id} ➔ 204")
    void testDeleteFound() throws Exception {
        Mockito.when(service.delete(1)).thenReturn(true);

        mvc.perform(delete("/api/creneaux/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/creneaux/{id} ➔ 404")
    void testDeleteNotFound() throws Exception {
        Mockito.when(service.delete(99)).thenReturn(false);

        mvc.perform(delete("/api/creneaux/99"))
                .andExpect(status().isNotFound());
    }
}
