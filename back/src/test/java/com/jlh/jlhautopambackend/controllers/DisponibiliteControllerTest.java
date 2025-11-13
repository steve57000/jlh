package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.modeles.Creneau;
import com.jlh.jlhautopambackend.modeles.Disponibilite;
import com.jlh.jlhautopambackend.modeles.DisponibiliteKey;
import com.jlh.jlhautopambackend.services.DisponibiliteService;
import com.jlh.jlhautopambackend.config.JwtAuthenticationFilter;
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

import java.util.Arrays;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = DisponibiliteController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class DisponibiliteControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DisponibiliteService service;

    // mocks JWT
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/disponibilites ➔ 200, json list")
    void testGetAll() throws Exception {
        Disponibilite d1 = Disponibilite.builder()
                .id(new DisponibiliteKey(1,100)).build();
        Disponibilite d2 = Disponibilite.builder()
                .id(new DisponibiliteKey(2,200)).build();

        Mockito.when(service.findAll()).thenReturn(Arrays.asList(d1, d2));

        mvc.perform(get("/api/disponibilites")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id.idAdmin").value(1))
                .andExpect(jsonPath("$[1].id.idCreneau").value(200));
    }

    @Test
    @DisplayName("GET /api/disponibilites/{adminId}/{creneauId} ➔ 200")
    void testGetByIdFound() throws Exception {
        DisponibiliteKey key = new DisponibiliteKey(3,300);
        Disponibilite d = Disponibilite.builder().id(key).build();
        Mockito.when(service.findByKey(3,300)).thenReturn(Optional.of(d));

        mvc.perform(get("/api/disponibilites/3/300")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id.idAdmin").value(3))
                .andExpect(jsonPath("$.id.idCreneau").value(300));
    }

    @Test
    @DisplayName("GET /api/disponibilites/{adminId}/{creneauId} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(service.findByKey(9,900)).thenReturn(Optional.empty());

        mvc.perform(get("/api/disponibilites/9/900")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/disponibilites ➔ 201 when both exist")
    void testCreateSuccess() throws Exception {
        int adminId = 4, creneauId = 400;
        Disponibilite input = Disponibilite.builder()
                .administrateur(Administrateur.builder().idAdmin(adminId).build())
                .creneau(Creneau.builder().idCreneau(creneauId).build())
                .build();
        Disponibilite saved = Disponibilite.builder()
                .id(new DisponibiliteKey(adminId, creneauId))
                .administrateur(input.getAdministrateur())
                .creneau(input.getCreneau())
                .build();

        Mockito.when(service.create(Mockito.eq(input))).thenReturn(saved);

        mvc.perform(post("/api/disponibilites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/disponibilites/4/400"))
                .andExpect(jsonPath("$.id.idAdmin").value(4))
                .andExpect(jsonPath("$.id.idCreneau").value(400));
    }

    @Test
    @DisplayName("POST /api/disponibilites ➔ 400 when admin missing")
    void testCreateBadRequestAdminMissing() throws Exception {
        Disponibilite input = Disponibilite.builder()
                .administrateur(Administrateur.builder().idAdmin(5).build())
                .creneau(Creneau.builder().idCreneau(500).build())
                .build();

        Mockito.when(service.create(Mockito.any()))
                .thenThrow(new IllegalArgumentException("Administrateur introuvable"));

        mvc.perform(post("/api/disponibilites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/disponibilites ➔ 400 when creneau missing")
    void testCreateBadRequestCreneauMissing() throws Exception {
        Disponibilite input = Disponibilite.builder()
                .administrateur(Administrateur.builder().idAdmin(6).build())
                .creneau(Creneau.builder().idCreneau(600).build())
                .build();

        Mockito.when(service.create(Mockito.any()))
                .thenThrow(new IllegalArgumentException("Créneau introuvable"));

        mvc.perform(post("/api/disponibilites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/disponibilites/{adminId}/{creneauId} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        Mockito.when(service.delete(7,700)).thenReturn(true);

        mvc.perform(delete("/api/disponibilites/7/700"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/disponibilites/{adminId}/{creneauId} ➔ 404 when not found")
    void testDeleteNotFound() throws Exception {
        Mockito.when(service.delete(8,800)).thenReturn(false);

        mvc.perform(delete("/api/disponibilites/8/800"))
                .andExpect(status().isNotFound());
    }
}
