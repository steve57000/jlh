package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.dto.StatutRendezVousDto;
import com.jlh.jlhautopambackend.services.StatutRendezVousService;
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
        controllers = StatutRendezVousController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class StatutRendezVousControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    private StatutRendezVousService service;

    // Mocks JWT si nécessaire
    @MockitoBean private com.jlh.jlhautopambackend.utils.JwtUtil jwtUtil;
    @MockitoBean private com.jlh.jlhautopambackend.config.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test @DisplayName("GET /api/statut-rendezvous ➔ 200")
    void testGetAll() throws Exception {
        StatutRendezVousDto d1 = StatutRendezVousDto.builder().codeStatut("X").libelle("One").build();
        StatutRendezVousDto d2 = StatutRendezVousDto.builder().codeStatut("Y").libelle("Two").build();
        Mockito.when(service.findAll()).thenReturn(List.of(d1,d2));

        mvc.perform(get("/api/statut-rendezvous"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codeStatut").value("X"))
                .andExpect(jsonPath("$[1].libelle").value("Two"));
    }

    @Test @DisplayName("GET /api/statut-rendezvous/{code} ➔ 200")
    void testGetByCodeFound() throws Exception {
        StatutRendezVousDto dto = StatutRendezVousDto.builder().codeStatut("Z").libelle("Zeta").build();
        Mockito.when(service.findByCode("Z")).thenReturn(Optional.of(dto));

        mvc.perform(get("/api/statut-rendezvous/Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("Zeta"));
    }

    @Test @DisplayName("GET /api/statut-rendezvous/{code} ➔ 404")
    void testGetByCodeNotFound() throws Exception {
        Mockito.when(service.findByCode("W")).thenReturn(Optional.empty());
        mvc.perform(get("/api/statut-rendezvous/W")).andExpect(status().isNotFound());
    }

    @Test @DisplayName("POST /api/statut-rendezvous ➔ 201")
    void testCreate() throws Exception {
        StatutRendezVousDto in = StatutRendezVousDto.builder().codeStatut("NEW").libelle("Nouveau").build();
        Mockito.when(service.create(Mockito.any())).thenReturn(in);

        mvc.perform(post("/api/statut-rendezvous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/statut-rendezvous/NEW"))
                .andExpect(jsonPath("$.codeStatut").value("NEW"));
    }

    @Test @DisplayName("PUT /api/statut-rendezvous/{code} ➔ 200")
    void testUpdateFound() throws Exception {
        StatutRendezVousDto in = StatutRendezVousDto.builder().libelle("Upd").build();
        StatutRendezVousDto out = StatutRendezVousDto.builder().codeStatut("C").libelle("Upd").build();
        Mockito.when(service.update(Mockito.eq("C"), Mockito.any())).thenReturn(Optional.of(out));

        mvc.perform(put("/api/statut-rendezvous/C")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("Upd"));
    }

    @Test @DisplayName("PUT /api/statut-rendezvous/{code} ➔ 404")
    void testUpdateNotFound() throws Exception {
        Mockito.when(service.update(Mockito.eq("N"), Mockito.any())).thenReturn(Optional.empty());
        mvc.perform(put("/api/statut-rendezvous/N")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(StatutRendezVousDto.builder().libelle("N").build())))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("DELETE /api/statut-rendezvous/{code} ➔ 204")
    void testDeleteFound() throws Exception {
        Mockito.when(service.delete("D")).thenReturn(true);
        mvc.perform(delete("/api/statut-rendezvous/D")).andExpect(status().isNoContent());
    }

    @Test @DisplayName("DELETE /api/statut-rendezvous/{code} ➔ 404")
    void testDeleteNotFound() throws Exception {
        Mockito.when(service.delete("Y")).thenReturn(false);
        mvc.perform(delete("/api/statut-rendezvous/Y")).andExpect(status().isNotFound());
    }
}
