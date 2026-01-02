package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.PromotionRequest;
import com.jlh.jlhautopambackend.dto.PromotionResponse;
import com.jlh.jlhautopambackend.mapper.PromotionMapper;
import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.modeles.Promotion;
import com.jlh.jlhautopambackend.repository.AdministrateurRepository;
import com.jlh.jlhautopambackend.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceImplTest {

    @Mock
    private PromotionRepository promoRepo;
    @Mock
    private AdministrateurRepository adminRepo;
    @Mock
    private PromotionMapper mapper;

    @InjectMocks
    private PromotionServiceImpl service;

    private PromotionRequest request;
    private Administrateur admin;
    private Promotion entityWithoutRel;
    private Promotion savedEntity;
    private PromotionResponse response;
    private Instant validFrom;
    private Instant validTo;
    private Instant invalidFrom;
    private Instant invalidTo;

    @BeforeEach
    void setUp() {
        validFrom = Instant.parse("2025-07-01T00:00:00Z");
        validTo = Instant.parse("2025-07-31T23:59:59Z");
        request = PromotionRequest.builder()
                .administrateurId(5)
                .imageUrl("http://img.jpg")
                .validFrom(validFrom)
                .validTo(validTo)
                .build();

        invalidFrom = Instant.parse("2025-08-01T00:00:00Z");
        invalidTo = Instant.parse("2025-07-01T00:00:00Z");

        admin = Administrateur.builder()
                .idAdmin(5)
                .build();

        entityWithoutRel = Promotion.builder()
                .imageUrl(request.getImageUrl())
                .validFrom(validFrom)
                .validTo(validTo)
                .build();

        savedEntity = Promotion.builder()
                .idPromotion(10)
                .administrateur(admin)
                .imageUrl(request.getImageUrl())
                .validFrom(validFrom)
                .validTo(validTo)
                .build();

        response = PromotionResponse.builder()
                .idPromotion(10)
                .administrateurId(5)
                .imageUrl(request.getImageUrl())
                .validFrom(validFrom)
                .validTo(validTo)
                .build();
    }

    @Test
    void testFindAll_ShouldReturnAllPromotions() {
        Promotion other = Promotion.builder()
                .idPromotion(11)
                .administrateur(admin)
                .imageUrl("url2")
                .validFrom(validFrom)
                .validTo(validTo)
                .build();
        PromotionResponse otherResp = PromotionResponse.builder()
                .idPromotion(11)
                .administrateurId(5)
                .imageUrl("url2")
                .validFrom(validFrom)
                .validTo(validTo)
                .build();

        when(promoRepo.findAll()).thenReturn(Arrays.asList(savedEntity, other));
        when(mapper.toResponse(savedEntity)).thenReturn(response);
        when(mapper.toResponse(other)).thenReturn(otherResp);

        List<PromotionResponse> results = service.findAll();

        assertEquals(2, results.size());
        assertEquals(response, results.get(0));
        assertEquals(otherResp, results.get(1));
        verify(promoRepo).findAll();
        verify(mapper).toResponse(savedEntity);
        verify(mapper).toResponse(other);
    }

    @Test
    void testFindById_WhenFound() {
        when(promoRepo.findById(10)).thenReturn(Optional.of(savedEntity));
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        Optional<PromotionResponse> result = service.findById(10);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
        verify(promoRepo).findById(10);
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    void testFindById_WhenNotFound() {
        when(promoRepo.findById(12)).thenReturn(Optional.empty());

        Optional<PromotionResponse> result = service.findById(12);

        assertFalse(result.isPresent());
        verify(promoRepo).findById(12);
    }

    @Test
    void testCreate_ShouldSetAdminAndReturnResponse() throws IOException {
        when(adminRepo.findById(5)).thenReturn(Optional.of(admin));
        when(mapper.toEntity(request)).thenReturn(entityWithoutRel);
        when(promoRepo.save(entityWithoutRel)).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        PromotionResponse result = service.create(request);

        assertEquals(response, result);
        ArgumentCaptor<Promotion> captor = ArgumentCaptor.forClass(Promotion.class);
        verify(adminRepo).findById(5);
        verify(mapper).toEntity(request);
        verify(promoRepo).save(captor.capture());
        Promotion passed = captor.getValue();
        assertEquals(admin, passed.getAdministrateur());
        assertEquals(request.getImageUrl(), passed.getImageUrl());
        assertEquals(validFrom, passed.getValidFrom());
        assertEquals(validTo, passed.getValidTo());
    }

    @Test
    void testCreate_ShouldThrowWhenAdminNotFound() throws IOException {
        when(adminRepo.findById(5)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(request)
        );
        assertEquals("Administrateur introuvable : 5", ex.getMessage());
        verify(adminRepo).findById(5);
        verifyNoMoreInteractions(promoRepo, mapper);
    }

    @Test
    void testUpdate_ShouldThrowWhenValidFromAfterValidTo() throws IOException {
        PromotionRequest badReq = PromotionRequest.builder()
                .administrateurId(5)
                .imageUrl(request.getImageUrl())
                .validFrom(invalidFrom)
                .validTo(invalidTo)
                .build();
        when(promoRepo.findById(10)).thenReturn(Optional.of(savedEntity));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(10, badReq)
        );
        assertEquals("validFrom doit être avant validTo", ex.getMessage());
        verify(promoRepo).findById(10);
    }

    @Test
    void testUpdate_WhenAdminUnchanged() throws IOException {
        PromotionRequest sameAdminReq = PromotionRequest.builder()
                .administrateurId(5)
                .imageUrl("newUrl")
                .validFrom(validFrom)
                .validTo(validTo)
                .build();
        Promotion existing = Promotion.builder()
                .idPromotion(10)
                .administrateur(admin)
                .imageUrl("oldUrl")
                .validFrom(validFrom)
                .validTo(validTo)
                .build();
        Promotion updatedEntity = Promotion.builder()
                .idPromotion(10)
                .administrateur(admin)
                .imageUrl("newUrl")
                .validFrom(validFrom)
                .validTo(validTo)
                .build();
        PromotionResponse updatedResp = PromotionResponse.builder()
                .idPromotion(10)
                .administrateurId(5)
                .imageUrl("newUrl")
                .validFrom(validFrom)
                .validTo(validTo)
                .build();

        when(promoRepo.findById(10)).thenReturn(Optional.of(existing));
        when(promoRepo.save(existing)).thenReturn(updatedEntity);
        when(mapper.toResponse(updatedEntity)).thenReturn(updatedResp);

        Optional<PromotionResponse> result = service.update(10, sameAdminReq);

        assertTrue(result.isPresent());
        assertEquals(updatedResp, result.get());
        verify(promoRepo).findById(10);
        verify(promoRepo).save(existing);
        verify(mapper).toResponse(updatedEntity);
        verifyNoInteractions(adminRepo);
    }

    @Test
    void testUpdate_WhenAdminChanged() throws IOException {
        PromotionRequest changeAdminReq = PromotionRequest.builder()
                .administrateurId(6)
                .imageUrl("urlX")
                .validFrom(validFrom)
                .validTo(validTo)
                .build();
        Administrateur newAdmin = Administrateur.builder().idAdmin(6).build();
        Promotion existing = savedEntity;
        Promotion updatedEntity = Promotion.builder()
                .idPromotion(10)
                .administrateur(newAdmin)
                .imageUrl("urlX")
                .validFrom(validFrom)
                .validTo(validTo)
                .build();
        PromotionResponse updatedResp = PromotionResponse.builder()
                .idPromotion(10)
                .administrateurId(6)
                .imageUrl("urlX")
                .validFrom(validFrom)
                .validTo(validTo)
                .build();

        when(promoRepo.findById(10)).thenReturn(Optional.of(existing));
        when(adminRepo.findById(6)).thenReturn(Optional.of(newAdmin));
        when(promoRepo.save(existing)).thenReturn(updatedEntity);
        when(mapper.toResponse(updatedEntity)).thenReturn(updatedResp);

        Optional<PromotionResponse> result = service.update(10, changeAdminReq);

        assertTrue(result.isPresent());
        assertEquals(updatedResp, result.get());
        verify(promoRepo).findById(10);
        verify(adminRepo).findById(6);
        verify(promoRepo).save(existing);
        verify(mapper).toResponse(updatedEntity);
    }

    @Test
    void testDelete_WhenExists() {
        when(promoRepo.existsById(10)).thenReturn(true);

        boolean result = service.delete(10);

        assertTrue(result);
        verify(promoRepo).existsById(10);
        verify(promoRepo).deleteById(10);
    }

    @Test
    void testDelete_WhenNotExists() {
        when(promoRepo.existsById(11)).thenReturn(false);

        boolean result = service.delete(11);

        assertFalse(result);
        verify(promoRepo).existsById(11);
        verify(promoRepo, never()).deleteById(anyInt());
    }
}
