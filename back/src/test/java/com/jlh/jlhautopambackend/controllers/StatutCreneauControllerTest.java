package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.dto.StatutCreneauDto;
import com.jlh.jlhautopambackend.services.StatutCreneauService;
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

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = StatutCreneauController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class StatutCreneauControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StatutCreneauService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/statuts-creneau ➔ 200, json list")
    void testGetAll() throws Exception {
        StatutCreneauDto s1 = StatutCreneauDto.builder().codeStatut("OK").libelle("Valid").build();
        StatutCreneauDto s2 = StatutCreneauDto.builder().codeStatut("KO").libelle("Invalid").build();
        Mockito.when(service.findAll()).thenReturn(List.of(s1, s2));

        mvc.perform(get("/api/statuts-creneau").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codeStatut").value("OK"))
                .andExpect(jsonPath("$[0].libelle").value("Valid"))
                .andExpect(jsonPath("$[1].codeStatut").value("KO"));
    }

    @Test
    @DisplayName("GET /api/statuts-creneau/{code} ➔ 200 when found")
    void testGetByCodeFound() throws Exception {
        StatutCreneauDto dto = StatutCreneauDto.builder().codeStatut("EX").libelle("Example").build();
        Mockito.when(service.findByCode("EX")).thenReturn(Optional.of(dto));

        mvc.perform(get("/api/statuts-creneau/EX").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeStatut").value("EX"))
                .andExpect(jsonPath("$.libelle").value("Example"));
    }

    @Test
    @DisplayName("GET /api/statuts-creneau/{code} ➔ 404 when not found")
    void testGetByCodeNotFound() throws Exception {
        Mockito.when(service.findByCode("NONE")).thenReturn(Optional.empty());

        mvc.perform(get("/api/statuts-creneau/NONE").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/statuts-creneau ➔ 201, JSON returned")
    void testCreate() throws Exception {
        StatutCreneauDto req = StatutCreneauDto.builder().codeStatut("NEW").libelle("NewEtat").build();
        StatutCreneauDto created = StatutCreneauDto.builder().codeStatut("NEW").libelle("NewEtat").build();
        Mockito.when(service.create(Mockito.any(StatutCreneauDto.class))).thenReturn(created);

        mvc.perform(post("/api/statuts-creneau")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/statuts-creneau/NEW"))
                .andExpect(jsonPath("$.codeStatut").value("NEW"));
    }

    @Test
    @DisplayName("POST /api/statuts-creneau ➔ 409 when conflict")
    void testCreateConflict() throws Exception {
        StatutCreneauDto req = StatutCreneauDto.builder().codeStatut("DUP").libelle("Duplicate").build();
        Mockito.when(service.create(Mockito.any(StatutCreneauDto.class)))
                .thenThrow(new IllegalStateException("Conflit"));

        mvc.perform(post("/api/statuts-creneau")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/statuts-creneau/{code} ➔ 200 when exists")
    void testUpdateFound() throws Exception {
        StatutCreneauDto req = StatutCreneauDto.builder().codeStatut("EX").libelle("Updated").build();
        StatutCreneauDto updated = StatutCreneauDto.builder().codeStatut("EX").libelle("Updated").build();
        Mockito.when(service.update(Mockito.eq("EX"), Mockito.any(StatutCreneauDto.class)))
                .thenReturn(Optional.of(updated));

        mvc.perform(put("/api/statuts-creneau/EX")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("Updated"));
    }

    @Test
    @DisplayName("PUT /api/statuts-creneau/{code} ➔ 404 when not found")
    void testUpdateNotFound() throws Exception {
        StatutCreneauDto req = StatutCreneauDto.builder().codeStatut("NF").libelle("X").build();
        Mockito.when(service.update(Mockito.eq("NF"), Mockito.any(StatutCreneauDto.class)))
                .thenReturn(Optional.empty());

        mvc.perform(put("/api/statuts-creneau/NF")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/statuts-creneau/{code} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        Mockito.when(service.delete("DEL")).thenReturn(true);

        mvc.perform(delete("/api/statuts-creneau/DEL"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/statuts-creneau/{code} ➔ 404 when not found")
    void testDeleteNotFound() throws Exception {
        Mockito.when(service.delete("XX")).thenReturn(false);

        mvc.perform(delete("/api/statuts-creneau/XX"))
                .andExpect(status().isNotFound());
    }
}
