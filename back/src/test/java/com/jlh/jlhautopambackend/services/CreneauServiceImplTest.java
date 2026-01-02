package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.CreneauRequest;
import com.jlh.jlhautopambackend.dto.CreneauResponse;
import com.jlh.jlhautopambackend.mapper.CreneauMapper;
import com.jlh.jlhautopambackend.modeles.Creneau;
import com.jlh.jlhautopambackend.repository.CreneauRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreneauServiceImplTest {

    @Mock
    private CreneauRepository repository;

    @Mock
    private CreneauMapper mapper;

    @InjectMocks
    private CreneauServiceImpl service;

    private CreneauRequest request;
    private Creneau entity;
    private Creneau savedEntity;
    private CreneauResponse response;
    private Instant start;
    private Instant end;

    @BeforeEach
    void setUp() {
        start = Instant.parse("2025-07-14T10:00:00Z");
        end = Instant.parse("2025-07-14T12:00:00Z");

        request = CreneauRequest.builder()
                .dateDebut(start)
                .dateFin(end)
                .codeStatut("STATUT1")
                .build();

        entity = Creneau.builder()
                .dateDebut(start)
                .dateFin(end)
                .build();

        savedEntity = Creneau.builder()
                .idCreneau(1)
                .dateDebut(start)
                .dateFin(end)
                .build();

        response = CreneauResponse.builder()
                .idCreneau(1)
                .dateDebut(start)
                .dateFin(end)
                .statut(null)
                .disponibilites(Collections.emptyList())
                .build();
    }

    @Test
    void testCreate_ShouldSaveAndReturnResponse() {
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        CreneauResponse result = service.create(request);

        assertEquals(response, result);
        verify(mapper).toEntity(request);
        verify(repository).save(entity);
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    void testFindById_WhenFound() {
        when(repository.findById(1)).thenReturn(Optional.of(savedEntity));
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        Optional<CreneauResponse> result = service.findById(1);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
        verify(repository).findById(1);
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    void testFindById_WhenNotFound() {
        when(repository.findById(2)).thenReturn(Optional.empty());

        Optional<CreneauResponse> result = service.findById(2);

        assertFalse(result.isPresent());
        verify(repository).findById(2);
        verifyNoInteractions(mapper);
    }

    @Test
    void testFindAll_ShouldReturnListOfResponses() {
        Creneau other = Creneau.builder()
                .idCreneau(2)
                .dateDebut(start)
                .dateFin(end)
                .build();
        CreneauResponse otherResp = CreneauResponse.builder()
                .idCreneau(2)
                .dateDebut(start)
                .dateFin(end)
                .statut(null)
                .disponibilites(Collections.emptyList())
                .build();

        when(repository.findAll()).thenReturn(Arrays.asList(savedEntity, other));
        when(mapper.toResponse(savedEntity)).thenReturn(response);
        when(mapper.toResponse(other)).thenReturn(otherResp);

        List<CreneauResponse> results = service.findAll();

        assertEquals(2, results.size());
        assertEquals(response, results.get(0));
        assertEquals(otherResp, results.get(1));
        verify(repository).findAll();
        verify(mapper).toResponse(savedEntity);
        verify(mapper).toResponse(other);
    }

    @Test
    void testUpdate_WhenExists() {
        CreneauRequest updateReq = CreneauRequest.builder()
                .dateDebut(start.plusSeconds(3600))
                .dateFin(end.plusSeconds(3600))
                .codeStatut("STATUT2")
                .build();

        Creneau existing = Creneau.builder()
                .idCreneau(3)
                .dateDebut(start)
                .dateFin(end)
                .build();

        Creneau updatedEntity = Creneau.builder()
                .idCreneau(3)
                .dateDebut(updateReq.getDateDebut())
                .dateFin(updateReq.getDateFin())
                .build();

        CreneauResponse updatedResp = CreneauResponse.builder()
                .idCreneau(3)
                .dateDebut(updateReq.getDateDebut())
                .dateFin(updateReq.getDateFin())
                .statut(null)
                .disponibilites(Collections.emptyList())
                .build();

        when(repository.findById(3)).thenReturn(Optional.of(existing));
        when(mapper.toEntity(updateReq)).thenReturn(Creneau.builder().statut(null).build());
        when(repository.save(existing)).thenReturn(updatedEntity);
        when(mapper.toResponse(updatedEntity)).thenReturn(updatedResp);

        Optional<CreneauResponse> result = service.update(3, updateReq);

        assertTrue(result.isPresent());
        assertEquals(updatedResp, result.get());
        verify(repository).findById(3);
        verify(mapper).toEntity(updateReq);
        verify(repository).save(existing);
        verify(mapper).toResponse(updatedEntity);
    }

    @Test
    void testUpdate_WhenNotExists() {
        when(repository.findById(4)).thenReturn(Optional.empty());

        Optional<CreneauResponse> result = service.update(4, request);

        assertFalse(result.isPresent());
        verify(repository).findById(4);
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    void testDelete_WhenExists() {
        when(repository.existsById(5)).thenReturn(true);

        boolean result = service.delete(5);

        assertTrue(result);
        verify(repository).existsById(5);
        verify(repository).deleteById(5);
    }

    @Test
    void testDelete_WhenNotExists() {
        when(repository.existsById(6)).thenReturn(false);

        boolean result = service.delete(6);

        assertFalse(result);
        verify(repository).existsById(6);
        verify(repository, never()).deleteById(anyInt());
    }
}
