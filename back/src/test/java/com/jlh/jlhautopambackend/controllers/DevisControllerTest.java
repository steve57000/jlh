package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.dto.DevisRequest;
import com.jlh.jlhautopambackend.dto.DevisResponse;
import com.jlh.jlhautopambackend.services.DevisService;
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
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = DevisController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class DevisControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DevisService devisService;

    // Mocks pour JWT
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/devis ➔ 200, json list")
    void testGetAll() throws Exception {
        DevisResponse r1 = DevisResponse.builder()
                .idDevis(10)
                .demandeId(1)
                .dateDevis(Instant.parse("2025-01-01T10:00:00Z"))
                .montantTotal(new BigDecimal("100.00"))
                .build();
        DevisResponse r2 = DevisResponse.builder()
                .idDevis(20)
                .demandeId(2)
                .dateDevis(Instant.parse("2025-01-02T11:00:00Z"))
                .montantTotal(new BigDecimal("200.00"))
                .build();

        Mockito.when(devisService.findAll()).thenReturn(Arrays.asList(r1, r2));

        mvc.perform(get("/api/devis").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idDevis").value(10))
                .andExpect(jsonPath("$[1].montantTotal").value(200.00));
    }

    @Test
    @DisplayName("GET /api/devis/{id} ➔ 200")
    void testGetByIdFound() throws Exception {
        DevisResponse resp = DevisResponse.builder()
                .idDevis(30)
                .demandeId(3)
                .dateDevis(Instant.parse("2025-01-03T12:00:00Z"))
                .montantTotal(new BigDecimal("300.50"))
                .build();
        Mockito.when(devisService.findById(30)).thenReturn(Optional.of(resp));

        mvc.perform(get("/api/devis/30").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDevis").value(30))
                .andExpect(jsonPath("$.montantTotal").value(300.50));
    }

    @Test
    @DisplayName("GET /api/devis/{id} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(devisService.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/devis/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/devis ➔ 201, JSON retourné")
    void testCreate() throws Exception {
        DevisRequest req = DevisRequest.builder()
                .demandeId(4)
                .dateDevis(Instant.parse("2025-01-04T08:00:00Z"))
                .montantTotal(new BigDecimal("400.00"))
                .build();
        DevisResponse created = DevisResponse.builder()
                .idDevis(40)
                .demandeId(4)
                .dateDevis(req.getDateDevis())
                .montantTotal(req.getMontantTotal())
                .build();

        Mockito.when(devisService.create(Mockito.any(DevisRequest.class))).thenReturn(created);

        mvc.perform(post("/api/devis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/devis/40"))
                .andExpect(jsonPath("$.idDevis").value(40));
    }

    @Test
    @DisplayName("POST /api/devis ➔ 400 when demande missing")
    void testCreateBadRequest() throws Exception {
        DevisRequest req = DevisRequest.builder()
                .demandeId(5)
                .dateDevis(Instant.now())
                .montantTotal(new BigDecimal("500.00"))
                .build();

        Mockito.doThrow(new IllegalArgumentException("Demande introuvable"))
                .when(devisService).create(Mockito.any(DevisRequest.class));

        mvc.perform(post("/api/devis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/devis/{id} ➔ 200, JSON mis à jour")
    void testUpdateFound() throws Exception {
        DevisRequest req = DevisRequest.builder()
                .demandeId(8)
                .dateDevis(Instant.parse("2025-02-07T07:00:00Z"))
                .montantTotal(new BigDecimal("800.00"))
                .build();
        DevisResponse updated = DevisResponse.builder()
                .idDevis(7)
                .demandeId(8)
                .dateDevis(req.getDateDevis())
                .montantTotal(req.getMontantTotal())
                .build();

        Mockito.when(devisService.update(Mockito.eq(7), Mockito.any(DevisRequest.class)))
                .thenReturn(Optional.of(updated));

        mvc.perform(put("/api/devis/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateDevis").value(req.getDateDevis().toString()))
                .andExpect(jsonPath("$.montantTotal").value(800.00));
    }

    @Test
    @DisplayName("PUT /api/devis/{id} ➔ 400 when update invalid")
    void testUpdateBadRequest() throws Exception {
        int id = 58;
        DevisRequest req = DevisRequest.builder()
                .demandeId(99)
                .dateDevis(Instant.now())
                .montantTotal(new BigDecimal("123.45"))
                .build();

        Mockito.doThrow(new IllegalArgumentException("Demande introuvable"))
                .when(devisService).update(Mockito.eq(id), Mockito.any(DevisRequest.class));

        mvc.perform(put("/api/devis/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/devis/{id} ➔ 404 when not found")
    void testUpdateNotFound() throws Exception {
        DevisRequest req = DevisRequest.builder().build();
        Mockito.when(devisService.update(Mockito.eq(99), Mockito.any(DevisRequest.class)))
                .thenReturn(Optional.empty());

        mvc.perform(put("/api/devis/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/devis/{id} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        Mockito.when(devisService.delete(11)).thenReturn(true);

        mvc.perform(delete("/api/devis/11"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/devis/{id} ➔ 404 when not found")
    void testDeleteNotFound() throws Exception {
        Mockito.when(devisService.delete(99)).thenReturn(false);

        mvc.perform(delete("/api/devis/99"))
                .andExpect(status().isNotFound());
    }
}
