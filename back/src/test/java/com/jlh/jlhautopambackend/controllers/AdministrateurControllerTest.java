package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.repositories.AdministrateurRepository;
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
import java.util.Collections;
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
    private AdministrateurRepository adminRepo;

    @Test
    @DisplayName("GET /api/administrateurs ➔ 200, json list")
    void testGetAll() throws Exception {
        Administrateur a1 = Administrateur.builder()
                .idAdmin(1)
                .username("alice")
                .motDePasse("password1")
                .nom("Alice")
                .prenom("A")
                .disponibilites(Collections.emptyList())
                .build();
        Administrateur a2 = Administrateur.builder()
                .idAdmin(2)
                .username("bob")
                .motDePasse("password2")
                .nom("Bob")
                .prenom("B")
                .disponibilites(Collections.emptyList())
                .build();

        Mockito.when(adminRepo.findAll()).thenReturn(Arrays.asList(a1, a2));

        mvc.perform(get("/api/administrateurs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].idAdmin").value(1))
                .andExpect(jsonPath("$[1].username").value("bob"));
    }

    @Test
    @DisplayName("GET /api/administrateurs/{id} ➔ 200")
    void testGetByIdFound() throws Exception {
        Administrateur a = Administrateur.builder()
                .idAdmin(1)
                .username("alice")
                .motDePasse("password1")
                .nom("Alice")
                .prenom("A")
                .disponibilites(Collections.emptyList())
                .build();
        Mockito.when(adminRepo.findById(1)).thenReturn(Optional.of(a));

        mvc.perform(get("/api/administrateurs/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    @DisplayName("GET /api/administrateurs/{id} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(adminRepo.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/administrateurs/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/administrateurs ➔ 201, Location header")
    void testCreate() throws Exception {
        Administrateur in = Administrateur.builder()
                .username("charlie")
                .motDePasse("pwd3")
                .nom("Charlie")
                .prenom("C")
                .disponibilites(Collections.emptyList())
                .build();
        Administrateur saved = Administrateur.builder()
                .idAdmin(3)
                .username("charlie")
                .motDePasse("pwd3")
                .nom("Charlie")
                .prenom("C")
                .disponibilites(Collections.emptyList())
                .build();
        Mockito.when(adminRepo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(post("/api/administrateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/administrateurs/3"))
                .andExpect(jsonPath("$.idAdmin").value(3))
                .andExpect(jsonPath("$.username").value("charlie"));
    }

    @Test
    @DisplayName("PUT /api/administrateurs/{id} ➔ 200 when exists")
    void testUpdateFound() throws Exception {
        Administrateur existing = Administrateur.builder()
                .idAdmin(1)
                .username("alice")
                .motDePasse("pwd1")
                .nom("Alice")
                .prenom("A")
                .disponibilites(Collections.emptyList())
                .build();
        Administrateur updates = Administrateur.builder()
                .username("alice2")
                .motDePasse("newpwd")
                .nom("Alice")
                .prenom("A2")
                .disponibilites(Collections.emptyList())
                .build();
        Administrateur saved = Administrateur.builder()
                .idAdmin(1)
                .username("alice2")
                .motDePasse("newpwd")
                .nom("Alice")
                .prenom("A2")
                .disponibilites(Collections.emptyList())
                .build();

        Mockito.when(adminRepo.findById(1)).thenReturn(Optional.of(existing));
        Mockito.when(adminRepo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(put("/api/administrateurs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice2"))
                .andExpect(jsonPath("$.motDePasse").value("newpwd"));
    }

    @Test
    @DisplayName("PUT /api/administrateurs/{id} ➔ 404 when not found")
    void testUpdateNotFound() throws Exception {
        Administrateur updates = Administrateur.builder()
                .username("doesnt")
                .motDePasse("none")
                .nom("No")
                .prenom("One")
                .disponibilites(Collections.emptyList())
                .build();
        Mockito.when(adminRepo.findById(42)).thenReturn(Optional.empty());

        mvc.perform(put("/api/administrateurs/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/administrateurs/{id} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        Mockito.when(adminRepo.existsById(1)).thenReturn(true);

        mvc.perform(delete("/api/administrateurs/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/administrateurs/{id} ➔ 404 when not found")
    void testDeleteNotFound() throws Exception {
        Mockito.when(adminRepo.existsById(99)).thenReturn(false);

        mvc.perform(delete("/api/administrateurs/99"))
                .andExpect(status().isNotFound());
    }
}
