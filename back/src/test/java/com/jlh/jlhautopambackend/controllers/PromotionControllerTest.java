package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.config.JwtAuthenticationFilter;
import com.jlh.jlhautopambackend.dto.PromotionRequest;
import com.jlh.jlhautopambackend.dto.PromotionResponse;
import com.jlh.jlhautopambackend.services.PromotionService;
import com.jlh.jlhautopambackend.utils.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Remarques :
 * - On importe bien jsonPath de MockMvcResultMatchers
 * - On utilise directement jsonPath() sans cast
 */
@WebMvcTest(
        controllers = PromotionController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class PromotionControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PromotionService service;

    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/promotions ➔ 200, json list")
    void testGetAll() throws Exception {
        PromotionResponse p1 = PromotionResponse.builder()
                .idPromotion(1)
                .administrateurId(10)
                .imageUrl("http://img1")
                .validFrom(Instant.parse("2025-06-01T00:00:00Z"))
                .validTo(Instant.parse("2025-06-07T23:59:00Z"))
                .description("Promo 1")
                .build();
        PromotionResponse p2 = PromotionResponse.builder()
                .idPromotion(2)
                .administrateurId(20)
                .imageUrl("http://img2")
                .validFrom(Instant.parse("2025-07-01T00:00:00Z"))
                .validTo(Instant.parse("2025-07-05T23:59:00Z"))
                .description("Promo 2")
                .build();

        when(service.findAll()).thenReturn(Arrays.asList(p1, p2));

        mvc.perform(MockMvcRequestBuilders.get("/api/promotions")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPromotion").value(1))
                .andExpect(jsonPath("$[0].description").value("Promo 1"))
                .andExpect(jsonPath("$[1].administrateurId").value(20))
                .andExpect(jsonPath("$[1].description").value("Promo 2"));
    }

    @Test
    @DisplayName("GET /api/promotions/{id} ➔ 200 when found")
    void testGetByIdFound() throws Exception {
        PromotionResponse p = PromotionResponse.builder()
                .idPromotion(3)
                .administrateurId(30)
                .imageUrl("http://img3")
                .validFrom(Instant.parse("2025-08-01T00:00:00Z"))
                .validTo(Instant.parse("2025-08-02T00:00:00Z"))
                .description("Unique promo")
                .build();
        when(service.findById(3)).thenReturn(Optional.of(p));

        mvc.perform(MockMvcRequestBuilders.get("/api/promotions/3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPromotion").value(3))
                .andExpect(jsonPath("$.description").value("Unique promo"));
    }

    @Test
    @DisplayName("GET /api/promotions/{id} ➔ 404 when not found")
    void testGetByIdNotFound() throws Exception {
        when(service.findById(99)).thenReturn(Optional.empty());

        mvc.perform(MockMvcRequestBuilders.get("/api/promotions/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/promotions ➔ 201 when valid")
    void testCreateSuccess() throws Exception {
        PromotionRequest req = PromotionRequest.builder()
                .administrateurId(5)
                .imageUrl("http://new")
                .validFrom(Instant.parse("2025-06-01T00:00:00Z"))
                .validTo(Instant.parse("2025-06-07T23:59:00Z"))
                .description("Nouvelle promo")
                .build();
        PromotionResponse saved = PromotionResponse.builder()
                .idPromotion(7)
                .administrateurId(5)
                .imageUrl(req.getImageUrl())
                .validFrom(req.getValidFrom())
                .validTo(req.getValidTo())
                .description(req.getDescription())
                .build();

        String json = objectMapper.writeValueAsString(req);
        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "data", "application/json", json.getBytes());
        MockMultipartFile filePart = new MockMultipartFile(
                "file", "promo.jpg", "image/jpeg", "dummyImage".getBytes());

        when(service.create(any(PromotionRequest.class), any())).thenReturn(saved);

        mvc.perform(MockMvcRequestBuilders.multipart("/api/promotions")
                        .file(dataPart)
                        .file(filePart)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/promotions/7"))
                .andExpect(jsonPath("$.idPromotion").value(7))
                .andExpect(jsonPath("$.description").value("Nouvelle promo"));
    }

    @Test
    @DisplayName("POST /api/promotions ➔ 400 when admin missing")
    void testCreateBadRequestAdminMissing() throws Exception {
        PromotionRequest req = PromotionRequest.builder()
                .administrateurId(8)
                .imageUrl("http://bad")
                .validFrom(Instant.now())
                .validTo(Instant.now().plusSeconds(3600))
                .description("Erreur")
                .build();
        String json = objectMapper.writeValueAsString(req);
        MockMultipartFile dataPart = new MockMultipartFile("data", "data", "application/json", json.getBytes());
        MockMultipartFile filePart = new MockMultipartFile("file", "f", "image/png", new byte[0]);

        when(service.create(any(), any()))
                .thenThrow(new IllegalArgumentException("Administrateur introuvable : 8"));

        mvc.perform(MockMvcRequestBuilders.multipart("/api/promotions")
                        .file(dataPart)
                        .file(filePart))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/promotions/{id} ➔ 200 when valid")
    void testUpdateSuccess() throws Exception {
        PromotionRequest req = PromotionRequest.builder()
                .administrateurId(11)
                .imageUrl("http://upd")
                .validFrom(Instant.parse("2025-06-02T00:00:00Z"))
                .validTo(Instant.parse("2025-06-06T00:00:00Z"))
                .description("Mise à jour")
                .build();
        PromotionResponse updated = PromotionResponse.builder()
                .idPromotion(11)
                .administrateurId(11)
                .imageUrl(req.getImageUrl())
                .validFrom(req.getValidFrom())
                .validTo(req.getValidTo())
                .description(req.getDescription())
                .build();

        String json = objectMapper.writeValueAsString(req);
        MockMultipartFile dataPart = new MockMultipartFile("data", "data", "application/json", json.getBytes());
        MockMultipartFile filePart = new MockMultipartFile("file", "new.jpg", "image/jpeg", new byte[0]);

        when(service.update(eq(11), any(), any())).thenReturn(Optional.of(updated));

        mvc.perform(MockMvcRequestBuilders.multipart("/api/promotions/11")
                        .file(dataPart)
                        .file(filePart)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Mise à jour"));
    }

    @Test
    @DisplayName("DELETE /api/promotions/{id} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        when(service.delete(13)).thenReturn(true);

        mvc.perform(MockMvcRequestBuilders.delete("/api/promotions/13"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/promotions/{id} ➔ 404 when not found")
    void testDeleteNotFound() throws Exception {
        when(service.delete(99)).thenReturn(false);

        mvc.perform(MockMvcRequestBuilders.delete("/api/promotions/99"))
                .andExpect(status().isNotFound());
    }
}
