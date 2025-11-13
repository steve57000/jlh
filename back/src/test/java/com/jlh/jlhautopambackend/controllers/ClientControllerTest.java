package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.dto.ClientRequest;
import com.jlh.jlhautopambackend.dto.ClientResponse;
import com.jlh.jlhautopambackend.services.ClientService;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ClientController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class ClientControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClientService service;

    @MockitoBean
    private com.jlh.jlhautopambackend.utils.JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/clients ➔ 200, JSON list non vide")
    void testGetAllNonEmpty() throws Exception {
        ClientResponse r1 = ClientResponse.builder()
                .idClient(1)
                .nom("Dupont").prenom("Jean")
                .email("jean.dupont@example.com")
                .telephone("0123456789")
                .adresseLigne1("1 rue A")
                .adresseLigne2("2 rue B")
                .ville("Metz")
                .codePostal("12345")
                .build();
        ClientResponse r2 = ClientResponse.builder()
                .idClient(2)
                .nom("Martin").prenom("Marie")
                .email("marie.martin@example.com")
                .telephone("0987654321")
                .adresseLigne1("1 rue A")
                .adresseLigne2("2 rue B")
                .ville("Metz")
                .codePostal("12345")
                .build();

        Mockito.when(service.findAll()).thenReturn(List.of(r1, r2));

        mvc.perform(get("/api/clients").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].idClient").value(1))
                .andExpect(jsonPath("$[1].prenom").value("Marie"));
    }

    @Test
    @DisplayName("GET /api/clients ➔ 200, JSON liste vide")
    void testGetAllEmpty() throws Exception {
        Mockito.when(service.findAll()).thenReturn(Collections.emptyList());

        mvc.perform(get("/api/clients").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/clients/{id} ➔ 200, JSON")
    void testGetByIdFound() throws Exception {
        ClientResponse resp = ClientResponse.builder()
                .idClient(5)
                .nom("Durand").prenom("Luc")
                .email("luc.durand@example.com")
                .telephone("0112233445")
                .adresseLigne1("1 rue A")
                .adresseLigne2("2 rue B")
                .ville("Metz")
                .codePostal("12345")
                .build();
        Mockito.when(service.findById(5)).thenReturn(Optional.of(resp));

        mvc.perform(get("/api/clients/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idClient").value(5))
                .andExpect(jsonPath("$.email").value("luc.durand@example.com"));
    }

    @Test
    @DisplayName("GET /api/clients/{id} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(service.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/clients/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/clients ➔ 201, retourne JSON")
    void testCreate() throws Exception {
        ClientRequest req = ClientRequest.builder()
                .nom("Petit").prenom("Anne")
                .email("anne.petit@example.com")
                .telephone("0223344556")
                .adresseLigne1("1 rue A")
                .adresseLigne2("2 rue B")
                .ville("Metz")
                .codePostal("12345")
                .build();
        ClientResponse created = ClientResponse.builder()
                .idClient(10)
                .nom(req.getNom()).prenom(req.getPrenom())
                .email(req.getEmail())
                .telephone(req.getTelephone())
                .adresseLigne1(req.getAdresseLigne1())
                .adresseLigne2(req.getAdresseLigne2())
                .ville(req.getVille())
                .codePostal(req.getCodePostal())
                .build();

        Mockito.when(service.create(Mockito.any(ClientRequest.class))).thenReturn(created);

        mvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/clients/10"))
                .andExpect(jsonPath("$.idClient").value(10))
                .andExpect(jsonPath("$.adresse").value("4 boulevard D"));
    }

    @Test
    @DisplayName("PUT /api/clients/{id} ➔ 200, retourne JSON mis à jour")
    void testUpdateFound() throws Exception {
        ClientRequest updates = ClientRequest.builder()
                .nom("NewNom").prenom("NewPrenom")
                .email("new@example.com")
                .telephone("1111111111")
                .adresseLigne1("1 rue A")
                .adresseLigne2("2 rue B")
                .ville("Metz")
                .codePostal("12345")
                .build();
        ClientResponse updated = ClientResponse.builder()
                .idClient(7)
                .nom("NewNom").prenom("NewPrenom")
                .email("new@example.com")
                .telephone("1111111111")
                .adresseLigne1("1 rue A")
                .adresseLigne2("2 rue B")
                .ville("Metz")
                .codePostal("12345")
                .build();

        Mockito.when(service.update(Mockito.eq(7), Mockito.any(ClientRequest.class)))
                .thenReturn(Optional.of(updated));

        mvc.perform(put("/api/clients/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idClient").value(7))
                .andExpect(jsonPath("$.telephone").value("1111111111"));
    }

    @Test
    @DisplayName("PUT /api/clients/{id} ➔ 404")
    void testUpdateNotFound() throws Exception {
        ClientRequest updates = ClientRequest.builder()
                .nom("X").prenom("Y")
                .email("x@y.com")
                .telephone("123")
                .adresseLigne1("A")
                .adresseLigne2("B")
                .ville("Metz")
                .codePostal("12345")
                .build();
        Mockito.when(service.update(Mockito.eq(42), Mockito.any(ClientRequest.class)))
                .thenReturn(Optional.empty());

        mvc.perform(put("/api/clients/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/clients/{id} ➔ 204, sans contenu")
    void testDeleteFound() throws Exception {
        Mockito.when(service.delete(8)).thenReturn(true);

        mvc.perform(delete("/api/clients/8"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/clients/{id} ➔ 404")
    void testDeleteNotFound() throws Exception {
        Mockito.when(service.delete(99)).thenReturn(false);

        mvc.perform(delete("/api/clients/99"))
                .andExpect(status().isNotFound());
    }
}
