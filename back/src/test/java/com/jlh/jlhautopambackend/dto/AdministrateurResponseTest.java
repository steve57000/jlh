package com.jlh.jlhautopambackend.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdministrateurResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Builder et getters")
    void testBuilderAndGetters() {
        List<DisponibiliteIdDto> dispos = Arrays.asList(
                DisponibiliteIdDto.builder().idAdmin(1).idCreneau(100).build(),
                DisponibiliteIdDto.builder().idAdmin(1).idCreneau(200).build()
        );

        AdministrateurResponse resp = AdministrateurResponse.builder()
                .idAdmin(42)
                .email("user42")
                .nom("Dupont")
                .prenom("Jean")
                .disponibilites(dispos)
                .build();

        assertEquals(42, resp.getIdAdmin());
        assertEquals("user42", resp.getEmail());
        assertEquals("user42", resp.getUsername());
        assertEquals("Dupont", resp.getNom());
        assertEquals("Jean", resp.getPrenom());
        assertSame(dispos, resp.getDisponibilites());
    }

    @Test
    @DisplayName("No-args + setters")
    void testNoArgsAndSetters() {
        AdministrateurResponse resp = new AdministrateurResponse();
        resp.setIdAdmin(7);
        resp.setEmail("u7");
        resp.setNom("Martin");
        resp.setPrenom("Marie");
        resp.setDisponibilites(List.of());

        assertEquals(7, resp.getIdAdmin());
        assertEquals("u7", resp.getEmail());
        assertEquals("u7", resp.getUsername());
        assertEquals("Martin", resp.getNom());
        assertEquals("Marie", resp.getPrenom());
        assertTrue(resp.getDisponibilites().isEmpty());
    }

    @Test
    @DisplayName("equals et hashCode")
    void testEqualsAndHashCode() {
        List<DisponibiliteIdDto> dispos = List.of(
                DisponibiliteIdDto.builder().idAdmin(2).idCreneau(300).build()
        );

        AdministrateurResponse a = AdministrateurResponse.builder()
                .idAdmin(2).email("u2").nom("Leroy").prenom("Luc")
                .disponibilites(dispos).build();

        AdministrateurResponse b = AdministrateurResponse.builder()
                .idAdmin(2).email("u2").nom("Leroy").prenom("Luc")
                .disponibilites(dispos).build();

        AdministrateurResponse c = AdministrateurResponse.builder()
                .idAdmin(3).email("u3").nom("Durand").prenom("Luc")
                .disponibilites(dispos).build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    @DisplayName("JSON (de)serialisation")
    void testJsonSerialization() throws Exception {
        List<DisponibiliteIdDto> dispos = Collections.singletonList(
                DisponibiliteIdDto.builder().idAdmin(5).idCreneau(500).build()
        );

        AdministrateurResponse original = AdministrateurResponse.builder()
                .idAdmin(5)
                .email("user5")
                .nom("Petit")
                .prenom("Anne")
                .disponibilites(dispos)
                .build();
        original.setUsername(original.getEmail());

        String json = objectMapper.writeValueAsString(original);
        AdministrateurResponse fromJson = objectMapper.readValue(json, AdministrateurResponse.class);

        assertEquals(original, fromJson);
    }
}
