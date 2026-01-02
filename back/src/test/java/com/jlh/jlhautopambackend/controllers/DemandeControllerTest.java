package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.DemandeRequest;
import com.jlh.jlhautopambackend.dto.DemandeResponse;
import com.jlh.jlhautopambackend.dto.StatutDemandeDto;
import com.jlh.jlhautopambackend.dto.TypeDemandeDto;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.services.DemandeService;
import com.jlh.jlhautopambackend.services.support.AuthenticatedClientResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemandeControllerTest {

    @Mock
    private DemandeService service;

    @Mock
    private AuthenticatedClientResolver clientResolver;

    @InjectMocks
    private DemandeController controller;

    private static DemandeResponse buildResponse(int id) {
        return DemandeResponse.builder()
                .idDemande(id)
                .services(List.of())
                .documents(List.of())
                .build();
    }

    @Test
    @DisplayName("GET /api/demandes ➔ 200")
    void getAll_ok() {
        when(service.findAll()).thenReturn(List.of(buildResponse(1), buildResponse(2)));

        List<DemandeResponse> result = controller.getAll();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getIdDemande());
        assertEquals(2, result.get(1).getIdDemande());
        verify(service).findAll();
    }

    @Test
    @DisplayName("GET /api/demandes/{id} ➔ 200")
    void getById_found() {
        DemandeResponse resp = DemandeResponse.builder()
                .idDemande(3)
                .dateDemande(Instant.parse("2025-01-03T12:00:00Z"))
                .services(List.of())
                .documents(List.of())
                .build();
        when(service.findById(3)).thenReturn(Optional.of(resp));

        ResponseEntity<DemandeResponse> result = controller.getById(3);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(3, result.getBody().getIdDemande());
        verify(service).findById(3);
    }

    @Test
    @DisplayName("GET /api/demandes/{id} ➔ 404")
    void getById_notFound() {
        when(service.findById(99)).thenReturn(Optional.empty());

        ResponseEntity<DemandeResponse> result = controller.getById(99);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
        verify(service).findById(99);
    }

    @Test
    @DisplayName("POST /api/demandes (CLIENT) ➔ 201")
    void createForClient_created() {
        String email = "test@client100.fr";
        DemandeRequest req = DemandeRequest.builder()
                .dateDemande(Instant.parse("2025-08-15T10:00:00Z"))
                .codeType("Devis")
                .codeStatut("En_attente")
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                email,
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
        );

        when(clientResolver.requireCurrentClient(auth)).thenReturn(Client.builder().idClient(1).email(email).build());

        DemandeResponse created = DemandeResponse.builder()
                .idDemande(10)
                .dateDemande(req.getDateDemande())
                .typeDemande(TypeDemandeDto.builder().codeType("Devis").libelle("Devis").build())
                .statutDemande(StatutDemandeDto.builder().codeStatut("En_attente").libelle("En attente").build())
                .services(List.of())
                .documents(List.of())
                .build();

        when(service.createForClient(eq(1), any(DemandeRequest.class))).thenReturn(created);

        ResponseEntity<?> result = controller.createForClient(auth, req);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals("/api/demandes/10", result.getHeaders().getLocation().toString());
        assertEquals(created, result.getBody());
        verify(service).createForClient(eq(1), any(DemandeRequest.class));
    }

    @Test
    @DisplayName("PUT /api/demandes/{id} ➔ 200")
    void update_ok() {
        DemandeRequest req = DemandeRequest.builder()
                .dateDemande(Instant.parse("2025-02-05T09:00:00Z"))
                .codeType("T4")
                .codeStatut("S4")
                .build();
        DemandeResponse updated = DemandeResponse.builder()
                .idDemande(5)
                .dateDemande(req.getDateDemande())
                .services(List.of())
                .documents(List.of())
                .build();
        when(service.update(eq(5), any(DemandeRequest.class))).thenReturn(Optional.of(updated));

        ResponseEntity<DemandeResponse> result = controller.update(5, req);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("2025-02-05T09:00:00Z", result.getBody().getDateDemande().toString());
        verify(service).update(eq(5), any(DemandeRequest.class));
    }

    @Test
    @DisplayName("PUT /api/demandes/{id} ➔ 404")
    void update_notFound() {
        DemandeRequest req = DemandeRequest.builder()
                .dateDemande(Instant.parse("2025-03-01T07:00:00Z"))
                .codeType("T1")
                .codeStatut("S1")
                .build();
        when(service.update(eq(99), any(DemandeRequest.class))).thenReturn(Optional.empty());

        ResponseEntity<DemandeResponse> result = controller.update(99, req);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
        verify(service).update(eq(99), any(DemandeRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/demandes/{id} ➔ 204")
    void delete_ok() {
        when(service.delete(1)).thenReturn(true);

        ResponseEntity<Void> result = controller.delete(1);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(service).delete(1);
    }

    @Test
    @DisplayName("DELETE /api/demandes/{id} ➔ 404")
    void delete_notFound() {
        when(service.delete(99)).thenReturn(false);

        ResponseEntity<Void> result = controller.delete(99);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(service).delete(99);
    }
}
