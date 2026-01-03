package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.dto.StatutDemandeDto;
import com.jlh.jlhautopambackend.services.StatutDemandeService;
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

import java.util.Arrays;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = StatutDemandeController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class StatutDemandeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StatutDemandeService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/statut-demandes ➔ 200")
    void testGetAll() throws Exception {
        StatutDemandeDto sd1 = new StatutDemandeDto("S1", "Lib1");
        StatutDemandeDto sd2 = new StatutDemandeDto("S2", "Lib2");
        Mockito.when(service.findAll()).thenReturn(Arrays.asList(sd1, sd2));

        mvc.perform(get("/api/statut-demandes").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codeStatut").value("S1"))
                .andExpect(jsonPath("$[1].libelle").value("Lib2"));
    }

    @Test
    @DisplayName("GET /api/statut-demandes/{code} ➔ 200")
    void testGetByCodeFound() throws Exception {
        StatutDemandeDto dto = new StatutDemandeDto("S3", "Lib3");
        Mockito.when(service.findByCode("S3")).thenReturn(Optional.of(dto));

        mvc.perform(get("/api/statut-demandes/S3").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeStatut").value("S3"));
    }

    @Test
    @DisplayName("GET /api/statut-demandes/{code} ➔ 404")
    void testGetByCodeNotFound() throws Exception {
        Mockito.when(service.findByCode("NX")).thenReturn(Optional.empty());

        mvc.perform(get("/api/statut-demandes/NX").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/statut-demandes ➔ 201")
    void testCreate() throws Exception {
        StatutDemandeDto in = new StatutDemandeDto("N1", "Nouveau");
        StatutDemandeDto out = new StatutDemandeDto("N1", "Nouveau");
        Mockito.when(service.create(Mockito.any())).thenReturn(out);

        mvc.perform(post("/api/statut-demandes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/statut-demandes/N1"))
                .andExpect(jsonPath("$.codeStatut").value("N1"));
    }

    @Test
    @DisplayName("PUT /api/statut-demandes/{code} ➔ 200")
    void testUpdateFound() throws Exception {
        StatutDemandeDto in = new StatutDemandeDto("U1", "Upd");
        StatutDemandeDto out = new StatutDemandeDto("U1", "Upd");
        Mockito.when(service.update(Mockito.eq("U1"), Mockito.any()))
                .thenReturn(Optional.of(out));

        mvc.perform(put("/api/statut-demandes/U1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("Upd"));
    }

    @Test
    @DisplayName("PUT /api/statut-demandes/{code} ➔ 404")
    void testUpdateNotFound() throws Exception {
        StatutDemandeDto in = new StatutDemandeDto("X1", "X");
        Mockito.when(service.update(Mockito.eq("X1"), Mockito.any()))
                .thenReturn(Optional.empty());

        mvc.perform(put("/api/statut-demandes/X1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/statut-demandes/{code} ➔ 204")
    void testDeleteFound() throws Exception {
        Mockito.when(service.delete("D1")).thenReturn(true);

        mvc.perform(delete("/api/statut-demandes/D1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/statut-demandes/{code} ➔ 404")
    void testDeleteNotFound() throws Exception {
        Mockito.when(service.delete("D2")).thenReturn(false);

        mvc.perform(delete("/api/statut-demandes/D2"))
                .andExpect(status().isNotFound());
    }
}
