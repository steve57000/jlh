package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.dto.RendezVousRequest;
import com.jlh.jlhautopambackend.dto.RendezVousResponse;
import com.jlh.jlhautopambackend.repository.AdministrateurRepository;
import com.jlh.jlhautopambackend.services.RendezVousService;
import com.jlh.jlhautopambackend.config.JwtAuthenticationFilter;
import com.jlh.jlhautopambackend.services.support.AuthenticatedClientResolver;
import com.jlh.jlhautopambackend.utils.JwtUtil;
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
        controllers = RendezVousController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class RendezVousControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private RendezVousService service;
    @MockitoBean private AdministrateurRepository adminRepository;
    @MockitoBean private AuthenticatedClientResolver clientResolver;
    // mocks JWT pour bypasser le filtre
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test @DisplayName("GET /api/rendezvous ➔ 200, json list")
    void testGetAll() throws Exception {
        RendezVousResponse r1 = RendezVousResponse.builder().idRdv(10).build();
        RendezVousResponse r2 = RendezVousResponse.builder().idRdv(20).build();
        Mockito.when(service.findAll()).thenReturn(List.of(r1, r2));

        mvc.perform(get("/api/rendezvous").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idRdv").value(10))
                .andExpect(jsonPath("$[1].idRdv").value(20));
    }

    @Test @DisplayName("GET /api/rendezvous/{id} ➔ 200")
    void testGetByIdFound() throws Exception {
        RendezVousResponse resp = RendezVousResponse.builder().idRdv(5).build();
        Mockito.when(service.findById(5)).thenReturn(Optional.of(resp));

        mvc.perform(get("/api/rendezvous/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idRdv").value(5));
    }

    @Test @DisplayName("GET /api/rendezvous/{id} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(service.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/rendezvous/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("POST /api/rendezvous ➔ 201, JSON retour")
    void testCreate() throws Exception {
        RendezVousRequest req = RendezVousRequest.builder()
                .demandeId(1)
                .creneauId(2)
                .administrateurId(3)
                .codeStatut("S")
                .build();
        RendezVousResponse created = RendezVousResponse.builder()
                .idRdv(7)
                .build();
        Mockito.when(service.createLibre(Mockito.any(RendezVousRequest.class), Mockito.any()))
                .thenReturn(created);

        mvc.perform(post("/api/rendezvous")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("client1").roles("CLIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/rendezvous/7"))
                .andExpect(jsonPath("$.idRdv").value(7));
    }

    @Test @DisplayName("PUT /api/rendezvous/{id} ➔ 200")
    void testUpdateFound() throws Exception {
        RendezVousRequest req = RendezVousRequest.builder()
                .demandeId(1)
                .creneauId(2)
                .administrateurId(3)
                .codeStatut("S")
                .build();
        RendezVousResponse updated = RendezVousResponse.builder().idRdv(8).build();
        Mockito.when(service.update(Mockito.eq(8), Mockito.any(RendezVousRequest.class)))
                .thenReturn(Optional.of(updated));

        mvc.perform(put("/api/rendezvous/8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idRdv").value(8));
    }

    @Test @DisplayName("PUT /api/rendezvous/{id} ➔ 404")
    void testUpdateNotFound() throws Exception {
        Mockito.when(service.update(Mockito.eq(99), Mockito.any(RendezVousRequest.class)))
                .thenReturn(Optional.empty());

        mvc.perform(put("/api/rendezvous/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(RendezVousRequest.builder()
                                .demandeId(1)
                                .creneauId(2)
                                .administrateurId(3)
                                .codeStatut("S")
                                .build())))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("DELETE /api/rendezvous/{id} ➔ 204")
    void testDeleteFound() throws Exception {
        Mockito.when(service.delete(11)).thenReturn(true);

        mvc.perform(delete("/api/rendezvous/11"))
                .andExpect(status().isNoContent());
    }

    @Test @DisplayName("DELETE /api/rendezvous/{id} ➔ 404")
    void testDeleteNotFound() throws Exception {
        Mockito.when(service.delete(99)).thenReturn(false);

        mvc.perform(delete("/api/rendezvous/99"))
                .andExpect(status().isNotFound());
    }
}
