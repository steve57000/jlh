package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.DevisRequest;
import com.jlh.jlhautopambackend.dto.DevisResponse;
import com.jlh.jlhautopambackend.mapper.DevisMapper;
import com.jlh.jlhautopambackend.modeles.Devis;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.repository.DevisRepository;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DevisServiceImplTest {

    @Mock
    private DevisRepository devisRepo;
    @Mock
    private DemandeRepository demandeRepo;
    @Mock
    private DevisMapper mapper;

    @InjectMocks
    private DevisServiceImpl service;

    private DevisRequest request;
    private Instant dateDevis;
    private BigDecimal montant;
    private Demande demande;
    private Devis entityWithoutRel;
    private Devis savedEntity;
    private DevisResponse response;

    @BeforeEach
    void setUp() {
        dateDevis = Instant.parse("2025-07-14T08:00:00Z");
        montant = new BigDecimal("1234.56");
        request = DevisRequest.builder()
                .demandeId(42)
                .dateDevis(dateDevis)
                .montantTotal(montant)
                .build();

        demande = Demande.builder()
                .idDemande(42)
                .build();

        entityWithoutRel = Devis.builder()
                .dateDevis(dateDevis)
                .montantTotal(montant)
                .build();

        savedEntity = Devis.builder()
                .idDevis(7)
                .demande(demande)
                .dateDevis(dateDevis)
                .montantTotal(montant)
                .build();

        response = DevisResponse.builder()
                .idDevis(7)
                .demandeId(42)
                .dateDevis(dateDevis)
                .montantTotal(montant)
                .build();
    }

    @Test
    void testFindAll_ShouldReturnListOfResponses() {
        Devis other = Devis.builder()
                .idDevis(8)
                .demande(demande)
                .dateDevis(dateDevis)
                .montantTotal(new BigDecimal("200.00"))
                .build();
        DevisResponse otherResp = DevisResponse.builder()
                .idDevis(8)
                .demandeId(42)
                .dateDevis(dateDevis)
                .montantTotal(new BigDecimal("200.00"))
                .build();

        when(devisRepo.findAll()).thenReturn(Arrays.asList(savedEntity, other));
        when(mapper.toResponse(savedEntity)).thenReturn(response);
        when(mapper.toResponse(other)).thenReturn(otherResp);

        List<DevisResponse> results = service.findAll();

        assertEquals(2, results.size());
        assertEquals(response, results.get(0));
        assertEquals(otherResp, results.get(1));
        verify(devisRepo).findAll();
        verify(mapper).toResponse(savedEntity);
        verify(mapper).toResponse(other);
    }

    @Test
    void testFindById_WhenFound() {
        when(devisRepo.findById(7)).thenReturn(Optional.of(savedEntity));
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        Optional<DevisResponse> result = service.findById(7);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
        verify(devisRepo).findById(7);
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    void testFindById_WhenNotFound() {
        when(devisRepo.findById(9)).thenReturn(Optional.empty());

        Optional<DevisResponse> result = service.findById(9);

        assertFalse(result.isPresent());
        verify(devisRepo).findById(9);
        verifyNoInteractions(mapper);
    }

    @Test
    void testCreate_ShouldSetDemandeAndReturnResponse() {
        when(demandeRepo.findById(42)).thenReturn(Optional.of(demande));
        when(mapper.toEntity(request)).thenReturn(entityWithoutRel);
        when(devisRepo.save(entityWithoutRel)).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        DevisResponse result = service.create(request);

        assertEquals(response, result);
        ArgumentCaptor<Devis> captor = ArgumentCaptor.forClass(Devis.class);
        verify(demandeRepo).findById(42);
        verify(mapper).toEntity(request);
        verify(devisRepo).save(captor.capture());
        Devis passed = captor.getValue();
        assertEquals(demande, passed.getDemande());
        assertEquals(dateDevis, passed.getDateDevis());
        assertEquals(montant, passed.getMontantTotal());
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    void testCreate_ShouldThrowWhenDemandeNotFound() {
        when(demandeRepo.findById(42)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(request)
        );
        assertEquals("Demande introuvable", ex.getMessage());
        verify(demandeRepo).findById(42);
        verifyNoMoreInteractions(mapper, devisRepo);
    }

    @Test
    void testUpdate_WhenExists() {
        BigDecimal newMontant = new BigDecimal("999.99");
        DevisRequest updateReq = DevisRequest.builder()
                .demandeId(42)
                .dateDevis(dateDevis)
                .montantTotal(newMontant)
                .build();
        Devis existing = Devis.builder()
                .idDevis(7)
                .demande(demande)
                .dateDevis(dateDevis)
                .montantTotal(montant)
                .build();
        Devis updatedEntity = Devis.builder()
                .idDevis(7)
                .demande(demande)
                .dateDevis(dateDevis)
                .montantTotal(newMontant)
                .build();
        DevisResponse updatedResp = DevisResponse.builder()
                .idDevis(7)
                .demandeId(42)
                .dateDevis(dateDevis)
                .montantTotal(newMontant)
                .build();

        when(devisRepo.findById(7)).thenReturn(Optional.of(existing));
        when(devisRepo.save(existing)).thenReturn(updatedEntity);
        when(mapper.toResponse(updatedEntity)).thenReturn(updatedResp);

        Optional<DevisResponse> result = service.update(7, updateReq);

        assertTrue(result.isPresent());
        assertEquals(updatedResp, result.get());
        verify(devisRepo).findById(7);
        verify(devisRepo).save(existing);
        verify(mapper).toResponse(updatedEntity);
    }

    @Test
    void testUpdate_WhenNotExists() {
        DevisRequest updateReq = DevisRequest.builder()
                .demandeId(42)
                .dateDevis(dateDevis)
                .montantTotal(montant)
                .build();
        when(devisRepo.findById(99)).thenReturn(Optional.empty());

        Optional<DevisResponse> result = service.update(99, updateReq);

        assertFalse(result.isPresent());
        verify(devisRepo).findById(99);
        verifyNoMoreInteractions(devisRepo, mapper);
    }

    @Test
    void testDelete_WhenExists() {
        when(devisRepo.existsById(7)).thenReturn(true);

        boolean result = service.delete(7);

        assertTrue(result);
        verify(devisRepo).existsById(7);
        verify(devisRepo).deleteById(7);
    }

    @Test
    void testDelete_WhenNotExists() {
        when(devisRepo.existsById(8)).thenReturn(false);

        boolean result = service.delete(8);

        assertFalse(result);
        verify(devisRepo).existsById(8);
        verify(devisRepo, never()).deleteById(anyInt());
    }
}
