package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.StatutCreneauDto;
import com.jlh.jlhautopambackend.mapper.StatutCreneauMapper;
import com.jlh.jlhautopambackend.modeles.StatutCreneau;
import com.jlh.jlhautopambackend.repository.StatutCreneauRepository;
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
class StatutCreneauServiceImplTest {

    @Mock
    private StatutCreneauRepository repo;

    @Mock
    private StatutCreneauMapper mapper;

    @InjectMocks
    private StatutCreneauServiceImpl service;

    private StatutCreneau entity;
    private StatutCreneauDto dto;

    @BeforeEach
    void setUp() {
        entity = StatutCreneau.builder()
                .codeStatut("S1")
                .libelle("Lib1")
                .build();
        dto = StatutCreneauDto.builder()
                .codeStatut("S1")
                .libelle("Lib1")
                .build();
    }

    @Test
    void testFindAll_ShouldReturnDtos() {
        StatutCreneau other = StatutCreneau.builder()
                .codeStatut("S2")
                .libelle("Lib2")
                .build();
        StatutCreneauDto otherDto = StatutCreneauDto.builder()
                .codeStatut("S2")
                .libelle("Lib2")
                .build();

        when(repo.findAll()).thenReturn(Arrays.asList(entity, other));
        when(mapper.toDto(entity)).thenReturn(dto);
        when(mapper.toDto(other)).thenReturn(otherDto);

        List<StatutCreneauDto> results = service.findAll();

        assertEquals(2, results.size());
        assertEquals(dto, results.get(0));
        assertEquals(otherDto, results.get(1));
        verify(repo).findAll();
    }

    @Test
    void testFindByCode_WhenFound() {
        when(repo.findById("S1")).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        Optional<StatutCreneauDto> result = service.findByCode("S1");

        assertTrue(result.isPresent());
        assertEquals(dto, result.get());
        verify(repo).findById("S1");
        verify(mapper).toDto(entity);
    }

    @Test
    void testFindByCode_WhenNotFound() {
        when(repo.findById("S1")).thenReturn(Optional.empty());

        Optional<StatutCreneauDto> result = service.findByCode("S1");

        assertFalse(result.isPresent());
        verify(repo).findById("S1");
        verifyNoInteractions(mapper);
    }

    @Test
    void testCreate_ShouldSaveAndReturnDto() {
        when(repo.existsById("S1")).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repo.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        StatutCreneauDto result = service.create(dto);

        assertEquals(dto, result);
        verify(repo).existsById("S1");
        verify(mapper).toEntity(dto);
        verify(repo).save(entity);
        verify(mapper).toDto(entity);
    }

    @Test
    void testCreate_ShouldThrowWhenExists() {
        when(repo.existsById("S1")).thenReturn(true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.create(dto)
        );
        assertEquals("Statut déjà existant : S1", ex.getMessage());
        verify(repo).existsById("S1");
        verifyNoMoreInteractions(mapper, repo);
    }

    @Test
    void testUpdate_WhenExists() {
        StatutCreneau updatedEntity = StatutCreneau.builder()
                .codeStatut("S1")
                .libelle("NewLib")
                .build();
        StatutCreneauDto updatedDto = StatutCreneauDto.builder()
                .codeStatut("S1")
                .libelle("NewLib")
                .build();
        when(repo.findById("S1")).thenReturn(Optional.of(entity));
        when(repo.save(entity)).thenReturn(updatedEntity);
        when(mapper.toDto(updatedEntity)).thenReturn(updatedDto);

        Optional<StatutCreneauDto> result = service.update("S1", updatedDto);

        assertTrue(result.isPresent());
        assertEquals(updatedDto, result.get());
        verify(repo).findById("S1");
        verify(repo).save(entity);
        verify(mapper).toDto(updatedEntity);
    }

    @Test
    void testUpdate_WhenNotExists() {
        StatutCreneauDto updatedDto = StatutCreneauDto.builder()
                .codeStatut("S1")
                .libelle("NewLib")
                .build();
        when(repo.findById("S1")).thenReturn(Optional.empty());

        Optional<StatutCreneauDto> result = service.update("S1", updatedDto);

        assertFalse(result.isPresent());
        verify(repo).findById("S1");
    }

    @Test
    void testDelete_WhenExists() {
        when(repo.existsById("S1")).thenReturn(true);

        boolean result = service.delete("S1");

        assertTrue(result);
        verify(repo).existsById("S1");
        verify(repo).deleteById("S1");
    }

    @Test
    void testDelete_WhenNotExists() {
        when(repo.existsById("S1")).thenReturn(false);

        boolean result = service.delete("S1");

        assertFalse(result);
        verify(repo).existsById("S1");
        verify(repo, never()).deleteById(anyString());
    }
}
