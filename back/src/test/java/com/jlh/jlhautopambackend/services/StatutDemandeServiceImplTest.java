package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.StatutDemandeDto;
import com.jlh.jlhautopambackend.mapper.StatutDemandeMapper;
import com.jlh.jlhautopambackend.modeles.StatutDemande;
import com.jlh.jlhautopambackend.repository.StatutDemandeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatutDemandeServiceImplTest {

    @Mock
    private StatutDemandeRepository repo;
    @Mock
    private StatutDemandeMapper mapper;

    @InjectMocks
    private StatutDemandeServiceImpl service;

    private StatutDemande entity;
    private StatutDemandeDto dto;

    @BeforeEach
    void setUp() {
        entity = StatutDemande.builder()
                .codeStatut("SD1")
                .libelle("En attente")
                .build();
        dto = StatutDemandeDto.builder()
                .codeStatut("SD1")
                .libelle("En attente")
                .build();
    }

    @Test
    void testFindAll_ShouldReturnDtos() {
        StatutDemande other = StatutDemande.builder()
                .codeStatut("SD2")
                .libelle("Traité")
                .build();
        StatutDemandeDto otherDto = StatutDemandeDto.builder()
                .codeStatut("SD2")
                .libelle("Traité")
                .build();

        when(repo.findAll()).thenReturn(Arrays.asList(entity, other));
        when(mapper.toDto(entity)).thenReturn(dto);
        when(mapper.toDto(other)).thenReturn(otherDto);

        List<StatutDemandeDto> results = service.findAll();

        assertEquals(2, results.size());
        assertEquals(dto, results.get(0));
        assertEquals(otherDto, results.get(1));
        verify(repo).findAll();
    }

    @Test
    void testFindByCode_WhenFound() {
        when(repo.findById("SD1")).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        Optional<StatutDemandeDto> result = service.findByCode("SD1");

        assertTrue(result.isPresent());
        assertEquals(dto, result.get());
        verify(repo).findById("SD1");
        verify(mapper).toDto(entity);
    }

    @Test
    void testFindByCode_WhenNotFound() {
        when(repo.findById("SD1")).thenReturn(Optional.empty());

        Optional<StatutDemandeDto> result = service.findByCode("SD1");

        assertFalse(result.isPresent());
        verify(repo).findById("SD1");
        verifyNoInteractions(mapper);
    }

    @Test
    void testCreate_ShouldSaveAndReturnDto() {
        when(repo.existsById("SD1")).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repo.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        StatutDemandeDto result = service.create(dto);

        assertEquals(dto, result);
        verify(repo).existsById("SD1");
        verify(mapper).toEntity(dto);
        verify(repo).save(entity);
        verify(mapper).toDto(entity);
    }

    @Test
    void testCreate_ShouldThrowWhenExists() {
        when(repo.existsById("SD1")).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.create(dto));
        assertEquals("Statut_demande déjà existant : SD1", ex.getMessage());
        verify(repo).existsById("SD1");
        verifyNoMoreInteractions(mapper, repo);
    }

    @Test
    void testUpdate_WhenExists() {
        StatutDemande updatedEntity = StatutDemande.builder()
                .codeStatut("SD1")
                .libelle("Confirmé")
                .build();
        StatutDemandeDto updatedDto = StatutDemandeDto.builder()
                .codeStatut("SD1")
                .libelle("Confirmé")
                .build();

        when(repo.findById("SD1")).thenReturn(Optional.of(entity));
        when(repo.save(entity)).thenReturn(updatedEntity);
        when(mapper.toDto(updatedEntity)).thenReturn(updatedDto);

        Optional<StatutDemandeDto> result = service.update("SD1", updatedDto);

        assertTrue(result.isPresent());
        assertEquals(updatedDto, result.get());
        verify(repo).findById("SD1");
        verify(repo).save(entity);
        verify(mapper).toDto(updatedEntity);
    }

    @Test
    void testUpdate_WhenNotExists() {
        when(repo.findById("SD1")).thenReturn(Optional.empty());
        StatutDemandeDto updatedDto = StatutDemandeDto.builder()
                .codeStatut("SD1")
                .libelle("Confirmé")
                .build();

        Optional<StatutDemandeDto> result = service.update("SD1", updatedDto);

        assertFalse(result.isPresent());
        verify(repo).findById("SD1");
    }

    @Test
    void testDelete_WhenExists() {
        when(repo.existsById("SD1")).thenReturn(true);

        boolean result = service.delete("SD1");

        assertTrue(result);
        verify(repo).existsById("SD1");
        verify(repo).deleteById("SD1");
    }

    @Test
    void testDelete_WhenNotExists() {
        when(repo.existsById("SD1")).thenReturn(false);

        boolean result = service.delete("SD1");

        assertFalse(result);
        verify(repo).existsById("SD1");
        verify(repo, never()).deleteById(anyString());
    }
}
