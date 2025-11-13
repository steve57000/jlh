package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.dto.DemandeRequest;
import com.jlh.jlhautopambackend.dto.DemandeResponse;
import com.jlh.jlhautopambackend.dto.StatutDemandeDto;
import com.jlh.jlhautopambackend.dto.TypeDemandeDto;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.services.DemandeService;
import com.jlh.jlhautopambackend.services.support.AuthenticatedClientResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DemandeController.class)
@Import(DemandeControllerTest.TestSecurity.class)
@ActiveProfiles("test")
class DemandeControllerTest {

    // Neutralise tout préfixe global qui casserait les routes pendant le test
    @DynamicPropertySource
    static void overridePaths(DynamicPropertyRegistry r) {
        r.add("server.servlet.context-path", () -> "");
        r.add("app.api-prefix", () -> "/api");
    }

    @Configuration
    @EnableMethodSecurity
    static class TestSecurity {
        @Bean
        SecurityFilterChain testChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(a -> a.anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    DemandeService service;

    @MockitoBean
    AuthenticatedClientResolver clientResolver;

    @Test
    @DisplayName("GET /api/demandes ➔ 200")
    @WithMockUser(roles = "ADMIN")
    void getAll_ok() throws Exception {
        Mockito.when(service.findAll()).thenReturn(List.of(
                DemandeResponse.builder().idDemande(1).services(List.of()).build(),
                DemandeResponse.builder().idDemande(2).services(List.of()).build()
        ));

        mvc.perform(get("/api/demandes").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idDemande").value(1))
                .andExpect(jsonPath("$[1].idDemande").value(2));
    }

    @Test
    @DisplayName("GET /api/demandes/{id} ➔ 200")
    @WithMockUser(roles = "ADMIN")
    void getById_found() throws Exception {
        var resp = DemandeResponse.builder()
                .idDemande(3).dateDemande(Instant.parse("2025-01-03T12:00:00Z"))
                .services(List.of()).build();

        Mockito.when(service.findById(3)).thenReturn(Optional.of(resp));

        mvc.perform(get("/api/demandes/3").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDemande").value(3));
    }

    @Test
    @DisplayName("GET /api/demandes/{id} ➔ 404")
    @WithMockUser(roles = "ADMIN")
    void getById_notFound() throws Exception {
        Mockito.when(service.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/demandes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/demandes (CLIENT) ➔ 201")
    void createForClient_created() throws Exception {
        final String email = "test@client100.fr";

        var req = DemandeRequest.builder()
                .dateDemande(Instant.parse("2025-08-15T10:00:00Z"))
                .codeType("Devis")
                .codeStatut("En_attente")
                .build();

        Mockito.when(clientResolver.requireCurrentClient(Mockito.any()))
                .thenReturn(Client.builder().idClient(1).email(email).build());

        var created = DemandeResponse.builder()
                .idDemande(10)
                .dateDemande(req.getDateDemande())
                .typeDemande(TypeDemandeDto.builder().codeType("Devis").libelle("Devis").build())
                .statutDemande(StatutDemandeDto.builder().codeStatut("En_attente").libelle("En attente").build())
                .services(List.of())
                .build();

        Mockito.when(service.createForClient(Mockito.eq(1), Mockito.any(DemandeRequest.class)))
                .thenReturn(created);

        mvc.perform(post("/api/demandes")
                        .with(user(email).roles("CLIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/demandes/10"))
                .andExpect(jsonPath("$.idDemande").value(10));
    }

    @Test
    @DisplayName("PUT /api/demandes/{id} ➔ 200")
    @WithMockUser(roles = "ADMIN")
    void update_ok() throws Exception {
        var req = DemandeRequest.builder()
                .dateDemande(Instant.parse("2025-02-05T09:00:00Z"))
                .codeType("T4").codeStatut("S4").build();

        var updated = DemandeResponse.builder()
                .idDemande(5).dateDemande(req.getDateDemande()).services(List.of()).build();

        Mockito.when(service.update(Mockito.eq(5), Mockito.any(DemandeRequest.class)))
                .thenReturn(Optional.of(updated));

        mvc.perform(put("/api/demandes/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateDemande").value("2025-02-05T09:00:00Z"));
    }

    @Test
    @DisplayName("PUT /api/demandes/{id} ➔ 404")
    @WithMockUser(roles = "ADMIN")
    void update_notFound() throws Exception {
        var req = DemandeRequest.builder()
                .dateDemande(Instant.parse("2025-03-01T07:00:00Z"))
                .codeType("T1").codeStatut("S1").build();

        Mockito.when(service.update(Mockito.eq(99), Mockito.any(DemandeRequest.class)))
                .thenReturn(Optional.empty());

        mvc.perform(put("/api/demandes/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/demandes/{id} ➔ 204")
    @WithMockUser(roles = "ADMIN")
    void delete_ok() throws Exception {
        Mockito.when(service.delete(1)).thenReturn(true);

        mvc.perform(delete("/api/demandes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/demandes/{id} ➔ 404")
    @WithMockUser(roles = "ADMIN")
    void delete_notFound() throws Exception {
        Mockito.when(service.delete(99)).thenReturn(false);

        mvc.perform(delete("/api/demandes/99"))
                .andExpect(status().isNotFound());
    }
}
