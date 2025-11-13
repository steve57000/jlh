package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ServiceRequest;
import com.jlh.jlhautopambackend.dto.ServiceResponse;
import com.jlh.jlhautopambackend.mapper.ServiceMapper;
import com.jlh.jlhautopambackend.modeles.DemandeService;
import com.jlh.jlhautopambackend.modeles.Service;
import com.jlh.jlhautopambackend.repository.DemandeServiceRepository;
import com.jlh.jlhautopambackend.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceServiceImplTest {

    @Mock
    private ServiceRepository repo;

    @Mock
    private DemandeServiceRepository demandeServiceRepository;

    @Mock
    private ServiceMapper mapper;

    @InjectMocks
    private ServiceServiceImpl service;

    private ServiceRequest request;
    private Service entityWithoutId;
    private Service savedEntity;
    private ServiceResponse response;
    private BigDecimal price;

    @BeforeEach
    void setUp() {
        price = new BigDecimal("49.99");
        request = ServiceRequest.builder()
                .libelle("Cleaning")
                .description("Home cleaning service")
                .prixUnitaire(price)
                .build();

        entityWithoutId = Service.builder()
                .libelle(request.getLibelle())
                .description(request.getDescription())
                .prixUnitaire(request.getPrixUnitaire())
                .archived(false)
                .build();

        savedEntity = Service.builder()
                .idService(1)
                .libelle(request.getLibelle())
                .description(request.getDescription())
                .prixUnitaire(request.getPrixUnitaire())
                .archived(false)
                .build();

        response = ServiceResponse.builder()
                .idService(savedEntity.getIdService())
                .libelle(savedEntity.getLibelle())
                .description(savedEntity.getDescription())
                .prixUnitaire(savedEntity.getPrixUnitaire())
                .archived(false)
                .build();
    }

    @Test
    void testFindAll_ShouldReturnListOfResponses() {
        Service other = Service.builder()
                .idService(2)
                .libelle("Gardening")
                .description("Garden maintenance")
                .prixUnitaire(new BigDecimal("59.99"))
                .archived(false)
                .build();
        ServiceResponse otherResp = ServiceResponse.builder()
                .idService(2)
                .libelle("Gardening")
                .description("Garden maintenance")
                .prixUnitaire(new BigDecimal("59.99"))
                .archived(false)
                .build();

        when(repo.findAllByArchivedFalseOrderByLibelleAsc()).thenReturn(List.of(savedEntity, other));
        when(mapper.toResponse(savedEntity)).thenReturn(response);
        when(mapper.toResponse(other)).thenReturn(otherResp);

        List<ServiceResponse> results = service.findAll();

        assertEquals(2, results.size());
        assertEquals(response, results.get(0));
        assertEquals(otherResp, results.get(1));
        verify(repo).findAllByArchivedFalseOrderByLibelleAsc();
        verify(mapper).toResponse(savedEntity);
        verify(mapper).toResponse(other);
    }

    @Test
    void testFindById_WhenFound() {
        when(repo.findByIdServiceAndArchivedFalse(1)).thenReturn(Optional.of(savedEntity));
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        Optional<ServiceResponse> result = service.findById(1);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
        verify(repo).findByIdServiceAndArchivedFalse(1);
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    void testFindById_WhenNotFound() {
        when(repo.findByIdServiceAndArchivedFalse(3)).thenReturn(Optional.empty());

        Optional<ServiceResponse> result = service.findById(3);

        assertFalse(result.isPresent());
        verify(repo).findByIdServiceAndArchivedFalse(3);
        verifyNoInteractions(mapper);
    }

    @Test
    void testCreate_ShouldSaveAndReturnResponse() {
        when(mapper.toEntity(request)).thenReturn(entityWithoutId);
        when(repo.save(entityWithoutId)).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        ServiceResponse result = service.create(request);

        assertEquals(response, result);
        verify(mapper).toEntity(request);
        verify(repo).save(entityWithoutId);
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    void testUpdate_WhenExists() {
        ServiceRequest updateReq = ServiceRequest.builder()
                .libelle("Deep Cleaning")
                .description("Intensive cleaning")
                .prixUnitaire(new BigDecimal("79.99"))
                .build();
        Service existing = Service.builder()
                .idService(1)
                .libelle("Cleaning")
                .description("Home cleaning")
                .prixUnitaire(price)
                .archived(true)
                .build();
        Service updatedEntity = Service.builder()
                .idService(1)
                .libelle(updateReq.getLibelle())
                .description(updateReq.getDescription())
                .prixUnitaire(updateReq.getPrixUnitaire())
                .archived(false)
                .build();
        ServiceResponse updatedResp = ServiceResponse.builder()
                .idService(1)
                .libelle(updateReq.getLibelle())
                .description(updateReq.getDescription())
                .prixUnitaire(updateReq.getPrixUnitaire())
                .archived(false)
                .build();

        var associations = List.of(
                DemandeService.builder().build(),
                DemandeService.builder().build()
        );

        when(repo.findById(1)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(updatedEntity);
        when(mapper.toResponse(updatedEntity)).thenReturn(updatedResp);
        when(demandeServiceRepository.findByService_IdService(1)).thenReturn(associations);

        Optional<ServiceResponse> result = service.update(1, updateReq);

        assertTrue(result.isPresent());
        assertEquals(updatedResp, result.get());
        assertFalse(existing.isArchived(), "update should reactivate service");
        verify(repo).findById(1);
        verify(demandeServiceRepository).findByService_IdService(1);
        verify(demandeServiceRepository).saveAll(associations);
        verify(repo).save(existing);
        verify(mapper).toResponse(updatedEntity);
    }

    @Test
    void testUpdate_WhenNotExists() {
        when(repo.findById(4)).thenReturn(Optional.empty());

        Optional<ServiceResponse> result = service.update(4, request);

        assertFalse(result.isPresent());
        verify(repo).findById(4);
        verifyNoInteractions(mapper);
        verifyNoInteractions(demandeServiceRepository);
    }

    @Test
    void testDelete_WhenExistsAndActive() {
        Service active = Service.builder()
                .idService(1)
                .libelle("Cleaning")
                .archived(false)
                .build();
        var associations = List.of(DemandeService.builder().build());
        when(repo.findById(1)).thenReturn(Optional.of(active));
        when(demandeServiceRepository.findByService_IdService(1)).thenReturn(associations);

        boolean result = service.delete(1);

        assertTrue(result);
        assertThat(active.isArchived()).isTrue();
        verify(repo).findById(1);
        verify(demandeServiceRepository).findByService_IdService(1);
        verify(demandeServiceRepository).saveAll(associations);
        verify(repo).save(active);
        verify(repo, never()).deleteById(any());
    }

    @Test
    void testDelete_WhenServiceAlreadyArchived() {
        Service archived = Service.builder()
                .idService(2)
                .libelle("Old")
                .archived(true)
                .build();
        when(repo.findById(2)).thenReturn(Optional.of(archived));

        boolean result = service.delete(2);

        assertTrue(result);
        verify(repo).findById(2);
        verifyNoInteractions(demandeServiceRepository);
        verify(repo, never()).deleteById(any());
        verify(repo, never()).save(any());
    }

    @Test
    void testDelete_WhenNotExists() {
        when(repo.findById(5)).thenReturn(Optional.empty());

        boolean result = service.delete(5);

        assertFalse(result);
        verify(repo).findById(5);
        verifyNoInteractions(demandeServiceRepository);
        verify(repo, never()).deleteById(anyInt());
    }
}
