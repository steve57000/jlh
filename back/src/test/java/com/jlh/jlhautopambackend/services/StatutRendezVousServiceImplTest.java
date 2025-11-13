package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.StatutRendezVousDto;
import com.jlh.jlhautopambackend.mapper.StatutRendezVousMapper;
import com.jlh.jlhautopambackend.modeles.StatutRendezVous;
import com.jlh.jlhautopambackend.repository.StatutRendezVousRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatutRendezVousServiceImplTest {
    @Mock
    private StatutRendezVousRepository repo;
    @Mock
    private StatutRendezVousMapper mapper;
    @InjectMocks
    private StatutRendezVousServiceImpl service;

    private StatutRendezVous entity;
    private StatutRendezVousDto dto;

    @BeforeEach
    void setUp() {
        entity = StatutRendezVous.builder()
                .codeStatut("SR1")
                .libelle("Pending")
                .build();
        dto = StatutRendezVousDto.builder()
                .codeStatut(entity.getCodeStatut())
                .libelle(entity.getLibelle())
                .build();
    }

    @Test
    void testFindAll_ShouldReturnDtos() {
        StatutRendezVous other = StatutRendezVous.builder()
                .codeStatut("SR2").libelle("Booked").build();
        StatutRendezVousDto otherDto = StatutRendezVousDto.builder()
                .codeStatut("SR2").libelle("Booked").build();

        when(repo.findAll()).thenReturn(Arrays.asList(entity, other));
        when(mapper.toDto(entity)).thenReturn(dto);
        when(mapper.toDto(other)).thenReturn(otherDto);

        List<StatutRendezVousDto> results = service.findAll();

        assertEquals(2, results.size());
        assertEquals(dto, results.get(0));
        assertEquals(otherDto, results.get(1));
        verify(repo).findAll();
        verify(mapper).toDto(entity);
        verify(mapper).toDto(other);
    }

    @Test
    void testFindAll_WhenEmpty() {
        when(repo.findAll()).thenReturn(Collections.emptyList());

        List<StatutRendezVousDto> results = service.findAll();

        assertTrue(results.isEmpty());
        verify(repo).findAll();
        verifyNoInteractions(mapper);
    }

    @Test
    void testFindByCode_WhenFound() {
        when(repo.findById("SR1")).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        Optional<StatutRendezVousDto> result = service.findByCode("SR1");

        assertTrue(result.isPresent());
        assertEquals(dto, result.get());
        verify(repo).findById("SR1");
        verify(mapper).toDto(entity);
    }

    @Test
    void testFindByCode_WhenNotFound() {
        when(repo.findById("SR1")).thenReturn(Optional.empty());

        Optional<StatutRendezVousDto> result = service.findByCode("SR1");

        assertFalse(result.isPresent());
        verify(repo).findById("SR1");
        verifyNoInteractions(mapper);
    }

    @Test
    void testCreate_ShouldSaveAndReturnDto() {
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repo.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        StatutRendezVousDto result = service.create(dto);

        assertEquals(dto, result);
        ArgumentCaptor<StatutRendezVous> captor = ArgumentCaptor.forClass(StatutRendezVous.class);
        verify(mapper).toEntity(dto);
        verify(repo).save(captor.capture());
        StatutRendezVous passed = captor.getValue();
        assertEquals(entity.getCodeStatut(), passed.getCodeStatut());
        assertEquals(entity.getLibelle(), passed.getLibelle());
        verify(mapper).toDto(entity);
    }

    @Test
    void testUpdate_WhenExists() {
        StatutRendezVousDto updatedDto = StatutRendezVousDto.builder()
                .codeStatut("SR1").libelle("Confirmed").build();
        StatutRendezVous updatedEntity = StatutRendezVous.builder()
                .codeStatut("SR1").libelle("Confirmed").build();

        when(repo.findById("SR1")).thenReturn(Optional.of(entity));
        // mapper.updateEntity invoked inside create, but we stub save and toDto only
        when(repo.save(entity)).thenReturn(updatedEntity);
        when(mapper.toDto(updatedEntity)).thenReturn(updatedDto);

        Optional<StatutRendezVousDto> result = service.update("SR1", updatedDto);

        assertTrue(result.isPresent());
        assertEquals(updatedDto, result.get());
        verify(repo).findById("SR1");
        verify(repo).save(entity);
        verify(mapper).toDto(updatedEntity);
    }

    @Test
    void testUpdate_WhenNotExists() {
        when(repo.findById("SR1")).thenReturn(Optional.empty());
        StatutRendezVousDto updatedDto = StatutRendezVousDto.builder()
                .codeStatut("SR1").libelle("Confirmed").build();

        Optional<StatutRendezVousDto> result = service.update("SR1", updatedDto);

        assertFalse(result.isPresent());
        verify(repo).findById("SR1");
        verifyNoMoreInteractions(repo, mapper);
    }

    @Test
    void testDelete_WhenExists() {
        when(repo.existsById("SR1")).thenReturn(true);

        boolean result = service.delete("SR1");

        assertTrue(result);
        verify(repo).existsById("SR1");
        verify(repo).deleteById("SR1");
    }

    @Test
    void testDelete_WhenNotExists() {
        when(repo.existsById("SR1")).thenReturn(false);

        boolean result = service.delete("SR1");

        assertFalse(result);
        verify(repo).existsById("SR1");
        verify(repo, never()).deleteById(anyString());
    }
}
