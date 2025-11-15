package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.repositories.ClientRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// On exclut la config de sécurité et on désactive les filtres
@WebMvcTest(
        controllers = ClientController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class ClientControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ClientRepository clientRepo;

    @Test
    @DisplayName("GET /api/clients ➔ 200, json list")
    void testGetAllClients() throws Exception {
        Client c1 = Client.builder()
                .idClient(1)
                .nom("Alice")
                .email("alice@example.com")
                .build();
        Client c2 = Client.builder()
                .idClient(2)
                .nom("Bob")
                .email("bob@example.com")
                .build();

        Mockito.when(clientRepo.findAll()).thenReturn(Arrays.asList(c1, c2));

        mvc.perform(get("/api/clients")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].idClient").value(1))
                .andExpect(jsonPath("$[1].nom").value("Bob"));
    }

    @Test
    @DisplayName("GET /api/clients/{id} ➔ 200, json client")
    void testGetClientByIdFound() throws Exception {
        Client c = Client.builder()
                .idClient(1)
                .nom("Alice")
                .email("alice@example.com")
                .build();
        Mockito.when(clientRepo.findById(1)).thenReturn(Optional.of(c));

        mvc.perform(get("/api/clients/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    @DisplayName("GET /api/clients/{id} ➔ 404 when not found")
    void testGetClientByIdNotFound() throws Exception {
        Mockito.when(clientRepo.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/clients/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
