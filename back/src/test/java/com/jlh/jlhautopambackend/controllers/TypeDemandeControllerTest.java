package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.dto.TypeDemandeDto;
import com.jlh.jlhautopambackend.services.TypeDemandeService;
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
        controllers = TypeDemandeController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class TypeDemandeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TypeDemandeService service;

    // ---- Mocks pour désactiver l'authentification JWT ----
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/types-demande ➔ 200, liste non vide")
    void testGetAll() throws Exception {
        TypeDemandeDto t1 = TypeDemandeDto.builder().codeType("A").libelle("Alpha").build();
        TypeDemandeDto t2 = TypeDemandeDto.builder().codeType("B").libelle("Beta").build();
        Mockito.when(service.findAll()).thenReturn(List.of(t1, t2));

        mvc.perform(get("/api/type-demandes").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codeType").value("A"))
                .andExpect(jsonPath("$[1].libelle").value("Beta"));
    }

    @Test
    @DisplayName("GET /api/types-demande/{code} ➔ 200 si existant")
    void testGetByCodeFound() throws Exception {
        TypeDemandeDto dto = TypeDemandeDto.builder().codeType("X").libelle("Xray").build();
        Mockito.when(service.findById("X")).thenReturn(Optional.of(dto));

        mvc.perform(get("/api/type-demandes/X").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("Xray"));
    }

    @Test
    @DisplayName("GET /api/types-demande/{code} ➔ 404 si absent")
    void testGetByCodeNotFound() throws Exception {
        Mockito.when(service.findById("Z")).thenReturn(Optional.empty());

        mvc.perform(get("/api/type-demandes/Z").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/types-demande ➔ 201 quand nouveau")
    void testCreateSuccess() throws Exception {
        TypeDemandeDto in = TypeDemandeDto.builder().codeType("C").libelle("Charlie").build();
        TypeDemandeDto saved = TypeDemandeDto.builder().codeType("C").libelle("Charlie").build();
        Mockito.when(service.create(Mockito.any())).thenReturn(saved);

        mvc.perform(post("/api/type-demandes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/type-demandes/C"))
                .andExpect(jsonPath("$.codeType").value("C"));
    }

    @Test
    @DisplayName("POST /api/type-demandes ➔ 409 si déjà existant")
    void testCreateConflict() throws Exception {
        TypeDemandeDto in = TypeDemandeDto.builder().codeType("D").libelle("Delta").build();
        Mockito.doThrow(new IllegalArgumentException())
                .when(service).create(Mockito.any());

        mvc.perform(post("/api/type-demandes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/types-demande/{code} ➔ 200 si existant")
    void testUpdateFound() throws Exception {
        TypeDemandeDto updates = TypeDemandeDto.builder().libelle("EchoUpdated").build();
        TypeDemandeDto saved   = TypeDemandeDto.builder().codeType("E").libelle("EchoUpdated").build();
        Mockito.when(service.update(Mockito.eq("E"), Mockito.any()))
                .thenReturn(Optional.of(saved));

        mvc.perform(put("/api/type-demandes/E")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("EchoUpdated"));
    }

    @Test
    @DisplayName("PUT /api/types-demande/{code} ➔ 404 si absent")
    void testUpdateNotFound() throws Exception {
        TypeDemandeDto updates = TypeDemandeDto.builder().libelle("Nope").build();
        Mockito.when(service.update(Mockito.eq("F"), Mockito.any()))
                .thenReturn(Optional.empty());

        mvc.perform(put("/api/type-demandes/F")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/types-demande/{code} ➔ 204 si existant")
    void testDeleteFound() throws Exception {
        Mockito.when(service.delete("G")).thenReturn(true);

        mvc.perform(delete("/api/type-demandes/G"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/types-demande/{code} ➔ 404 si absent")
    void testDeleteNotFound() throws Exception {
        Mockito.when(service.delete("H")).thenReturn(false);

        mvc.perform(delete("/api/type-demandes/H"))
                .andExpect(status().isNotFound());
    }
}
