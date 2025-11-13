package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.config.JwtAuthenticationFilter;
import com.jlh.jlhautopambackend.dto.AdministrateurRequest;
import com.jlh.jlhautopambackend.dto.AdministrateurResponse;
import com.jlh.jlhautopambackend.services.AdministrateurService;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AdministrateurController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class AdministrateurControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdministrateurService service;

    @MockitoBean
    private com.jlh.jlhautopambackend.utils.JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/administrateurs ➔ 200, json list")
    void testGetAll() throws Exception {
        AdministrateurResponse r1 = AdministrateurResponse.builder()
                .idAdmin(1)
                .email("alice")
                .nom("Alice").prenom("A")
                .disponibilites(Collections.emptyList())
                .build();
        AdministrateurResponse r2 = AdministrateurResponse.builder()
                .idAdmin(2)
                .email("bob")
                .nom("Bob").prenom("B")
                .disponibilites(Collections.emptyList())
                .build();

        Mockito.when(service.findAll()).thenReturn(List.of(r1, r2));

        mvc.perform(get("/api/administrateurs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].idAdmin").value(1))
                .andExpect(jsonPath("$[1].username").value("bob"));
    }

    @Test
    @DisplayName("GET /api/administrateurs/{id} ➔ 200")
    void testGetByIdFound() throws Exception {
        AdministrateurResponse resp = AdministrateurResponse.builder()
                .idAdmin(1)
                .email("alice")
                .nom("Alice").prenom("A")
                .disponibilites(Collections.emptyList())
                .build();

        Mockito.when(service.findById(1)).thenReturn(Optional.of(resp));

        mvc.perform(get("/api/administrateurs/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    @DisplayName("GET /api/administrateurs/{id} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(service.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/administrateurs/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/administrateurs ➔ 201, Location header")
    void testCreate() throws Exception {
        AdministrateurRequest req = AdministrateurRequest.builder()
                .email("charlie")
                .motDePasse("pwd3")
                .nom("Charlie").prenom("C")
                .build();
        AdministrateurResponse created = AdministrateurResponse.builder()
                .idAdmin(3)
                .email("Charlie").prenom("C")
                .disponibilites(Collections.emptyList())
                .build();

        Mockito.when(service.create(Mockito.any(AdministrateurRequest.class)))
                .thenReturn(created);

        mvc.perform(post("/api/administrateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/administrateurs/3"))
                .andExpect(jsonPath("$.idAdmin").value(3))
                .andExpect(jsonPath("$.username").value("charlie"));
    }

    @Test
    @DisplayName("PUT /api/administrateurs/{id} ➔ 200 when exists")
    void testUpdateFound() throws Exception {
        AdministrateurRequest updates = AdministrateurRequest.builder()
                .email("alice2")
                .motDePasse("newpwd")
                .nom("Alice").prenom("A2")
                .build();
        AdministrateurResponse updated = AdministrateurResponse.builder()
                .idAdmin(1)
                .email("alice2")
                .nom("Alice").prenom("A2")
                .disponibilites(Collections.emptyList())
                .build();

        Mockito.when(service.update(Mockito.eq(1), Mockito.any(AdministrateurRequest.class)))
                .thenReturn(Optional.of(updated));

        mvc.perform(put("/api/administrateurs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice2"));
    }

    @Test
    @DisplayName("PUT /api/administrateurs/{id} ➔ 404 when not found")
    void testUpdateNotFound() throws Exception {
        AdministrateurRequest updates = AdministrateurRequest.builder()
                .email("doesnt")
                .motDePasse("none")
                .nom("No").prenom("One")
                .build();

        Mockito.when(service.update(Mockito.eq(42), Mockito.any(AdministrateurRequest.class)))
                .thenReturn(Optional.empty());

        mvc.perform(put("/api/administrateurs/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/administrateurs/{id} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        Mockito.when(service.delete(1)).thenReturn(true);

        mvc.perform(delete("/api/administrateurs/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/administrateurs/{id} ➔ 404 when not found")
    void testDeleteNotFound() throws Exception {
        Mockito.when(service.delete(99)).thenReturn(false);

        mvc.perform(delete("/api/administrateurs/99"))
                .andExpect(status().isNotFound());
    }
}
