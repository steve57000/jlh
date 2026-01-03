package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.TypeDemandeDto;
import com.jlh.jlhautopambackend.mapper.TypeDemandeMapper;
import com.jlh.jlhautopambackend.modeles.TypeDemande;
import com.jlh.jlhautopambackend.repository.TypeDemandeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class TypeDemandeServiceImplTest {

    @Mock
    private TypeDemandeRepository repo;
    @Mock
    private TypeDemandeMapper mapper;

    @InjectMocks
    private TypeDemandeServiceImpl service;

    private TypeDemande entity;
    private TypeDemandeDto dto;

    @BeforeEach
    void setUp() {
        entity = TypeDemande.builder()
                .codeType("T1")
                .libelle("Type 1")
                .build();
        dto = TypeDemandeDto.builder()
                .codeType("T1")
                .libelle("Type 1")
                .build();
    }

    @Test
    void testFindAll_WhenNonEmpty() {
        TypeDemande other = TypeDemande.builder()
                .codeType("T2")
                .libelle("Type 2")
                .build();
        TypeDemandeDto otherDto = TypeDemandeDto.builder()
                .codeType("T2")
                .libelle("Type 2")
                .build();

        when(repo.findAll()).thenReturn(Arrays.asList(entity, other));
        when(mapper.toDto(entity)).thenReturn(dto);
        when(mapper.toDto(other)).thenReturn(otherDto);

        List<TypeDemandeDto> results = service.findAll();

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

        List<TypeDemandeDto> results = service.findAll();

        assertTrue(results.isEmpty());
        verify(repo).findAll();
        verifyNoInteractions(mapper);
    }

    @Test
    void testFindById_WhenFound() {
        when(repo.findById("T1")).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        Optional<TypeDemandeDto> result = service.findById("T1");

        assertTrue(result.isPresent());
        assertEquals(dto, result.get());
        verify(repo).findById("T1");
        verify(mapper).toDto(entity);
    }

    @Test
    void testFindById_WhenNotFound() {
        when(repo.findById("T1")).thenReturn(Optional.empty());

        Optional<TypeDemandeDto> result = service.findById("T1");

        assertFalse(result.isPresent());
        verify(repo).findById("T1");
        verifyNoInteractions(mapper);
    }

    @Test
    void testCreate_WhenNotExists() {
        when(repo.existsById(dto.getCodeType())).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repo.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        TypeDemandeDto result = service.create(dto);

        assertEquals(dto, result);
        verify(repo).existsById("T1");
        verify(mapper).toEntity(dto);
        verify(repo).save(entity);
        verify(mapper).toDto(entity);
    }

    @Test
    void testCreate_WhenExists_ShouldThrow() {
        when(repo.existsById(dto.getCodeType())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(dto)
        );
        assertEquals("TypeDemande déjà existant", ex.getMessage());
        verify(repo).existsById("T1");
        verifyNoMoreInteractions(repo, mapper);
    }

    @Test
    void testUpdate_WhenExists() {
        TypeDemandeDto updatedDto = TypeDemandeDto.builder()
                .codeType("T1")
                .libelle("Updated")
                .build();
        TypeDemande updatedEntity = TypeDemande.builder()
                .codeType("T1")
                .libelle("Updated")
                .build();

        when(repo.findById("T1")).thenReturn(Optional.of(entity));
        when(repo.save(entity)).thenReturn(updatedEntity);
        when(mapper.toDto(updatedEntity)).thenReturn(updatedDto);

        Optional<TypeDemandeDto> result = service.update("T1", updatedDto);

        assertTrue(result.isPresent());
        assertEquals(updatedDto, result.get());
        verify(repo).findById("T1");
        verify(repo).save(entity);
        verify(mapper).toDto(updatedEntity);
    }

    @Test
    void testUpdate_WhenNotExists() {
        TypeDemandeDto updatedDto = TypeDemandeDto.builder()
                .codeType("T1")
                .libelle("Updated")
                .build();
        when(repo.findById("T1")).thenReturn(Optional.empty());

        Optional<TypeDemandeDto> result = service.update("T1", updatedDto);

        assertFalse(result.isPresent());
        verify(repo).findById("T1");
        verifyNoMoreInteractions(repo, mapper);
    }

    @Test
    void testDelete_WhenExists() {
        when(repo.existsById("T1")).thenReturn(true);

        boolean result = service.delete("T1");

        assertTrue(result);
        verify(repo).existsById("T1");
        verify(repo).deleteById("T1");
    }

    @Test
    void testDelete_WhenNotExists() {
        when(repo.existsById("T1")).thenReturn(false);

        boolean result = service.delete("T1");

        assertFalse(result);
        verify(repo).existsById("T1");
        verify(repo, never()).deleteById(anyString());
    }
}
