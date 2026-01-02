package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.AdministrateurRequest;
import com.jlh.jlhautopambackend.dto.AdministrateurResponse;
import com.jlh.jlhautopambackend.mapper.AdministrateurMapper;
import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.repository.AdministrateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministrateurServiceImplTest {

    @Mock
    private AdministrateurRepository repository;

    @Mock
    private AdministrateurMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdministrateurServiceImpl service;

    private AdministrateurRequest request;
    private Administrateur entity;
    private Administrateur savedEntity;
    private AdministrateurResponse response;

    @BeforeEach
    void setUp() {
        // Common test data
        request = AdministrateurRequest.builder()
                .email("adminUser")
                .username(null)
                .motDePasse("rawPass")
                .nom("Dupont")
                .prenom("Jean")
                .build();

        entity = Administrateur.builder()
                .email(request.getEmail())
                .username(null)
                .motDePasse(null)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .build();

        savedEntity = Administrateur.builder()
                .idAdmin(1)
                .email(request.getEmail())
                .username(request.getEmail())
                .motDePasse("encodedPass")
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .build();

        response = AdministrateurResponse.builder()
                .idAdmin(savedEntity.getIdAdmin())
                .email(savedEntity.getEmail())
                .username(savedEntity.getUsername())
                .nom(savedEntity.getNom())
                .prenom(savedEntity.getPrenom())
                .disponibilites(Collections.emptyList())
                .build();
    }

    @Test
    void testCreate_ShouldEncodePasswordAndReturnResponse() {
        when(mapper.toEntity(request)).thenReturn(entity);
        when(passwordEncoder.encode(request.getMotDePasse())).thenReturn("encodedPass");
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        AdministrateurResponse result = service.create(request);

        assertEquals(response, result);
        verify(mapper).toEntity(request);
        verify(passwordEncoder).encode("rawPass");
        assertEquals("adminUser", entity.getUsername());
        assertEquals("encodedPass", entity.getMotDePasse());
        verify(repository).save(entity);
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    void testFindById_WhenFound() {
        when(repository.findById(1)).thenReturn(Optional.of(savedEntity));
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        Optional<AdministrateurResponse> result = service.findById(1);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
        verify(repository).findById(1);
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    void testFindById_WhenNotFound() {
        when(repository.findById(2)).thenReturn(Optional.empty());

        Optional<AdministrateurResponse> result = service.findById(2);

        assertFalse(result.isPresent());
        verify(repository).findById(2);
        verifyNoInteractions(mapper);
    }

    @Test
    void testFindAll_ShouldReturnListOfResponses() {
        Administrateur other = Administrateur.builder()
                .idAdmin(2)
                .email("user2")
                .username("user2")
                .motDePasse("pwd2")
                .nom("Martin")
                .prenom("Paul")
                .build();
        AdministrateurResponse otherResp = AdministrateurResponse.builder()
                .idAdmin(2)
                .email("user2")
                .username("user2")
                .nom("Martin")
                .prenom("Paul")
                .disponibilites(Collections.emptyList())
                .build();

        when(repository.findAll()).thenReturn(Arrays.asList(savedEntity, other));
        when(mapper.toResponse(savedEntity)).thenReturn(response);
        when(mapper.toResponse(other)).thenReturn(otherResp);

        List<AdministrateurResponse> results = service.findAll();

        assertEquals(2, results.size());
        assertEquals(response, results.get(0));
        assertEquals(otherResp, results.get(1));
        verify(repository).findAll();
        verify(mapper).toResponse(savedEntity);
        verify(mapper).toResponse(other);
    }

    @Test
    void testUpdate_WhenExistsAndPasswordProvided() {
        AdministrateurRequest updateReq = AdministrateurRequest.builder()
                .email("newUser")
                .username(null)
                .motDePasse("newPass")
                .nom("NewNom")
                .prenom("NewPrenom")
                .build();

        Administrateur existing = Administrateur.builder()
                .idAdmin(3)
                .email("oldUser")
                .username("oldUser")
                .motDePasse("oldPass")
                .nom("OldNom")
                .prenom("OldPrenom")
                .build();

        Administrateur updatedEntity = Administrateur.builder()
                .idAdmin(3)
                .email("newUser")
                .username("newUser")
                .motDePasse("encodedNewPass")
                .nom("NewNom")
                .prenom("NewPrenom")
                .build();

        AdministrateurResponse updatedResp = AdministrateurResponse.builder()
                .idAdmin(3)
                .email("newUser")
                .username("newUser")
                .nom("NewNom")
                .prenom("NewPrenom")
                .disponibilites(Collections.emptyList())
                .build();

        when(repository.findById(3)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");
        when(repository.save(existing)).thenReturn(updatedEntity);
        when(mapper.toResponse(updatedEntity)).thenReturn(updatedResp);

        Optional<AdministrateurResponse> result = service.update(3, updateReq);

        assertTrue(result.isPresent());
        assertEquals(updatedResp, result.get());
        assertEquals("newUser", existing.getUsername());
        verify(repository).findById(3);
        verify(passwordEncoder).encode("newPass");
        verify(repository).save(existing);
        verify(mapper).toResponse(updatedEntity);
    }

    @Test
    void testUpdate_WhenExistsAndPasswordBlank() {
        AdministrateurRequest updateReq = AdministrateurRequest.builder()
                .email("userNoPwd")
                .motDePasse("")  // motDePasse vide
                .nom("NomNoPwd")
                .prenom("PrenomNoPwd")
                .build();

        Administrateur existing = Administrateur.builder()
                .idAdmin(4)
                .email("origUser")
                .username("origUser")
                .motDePasse("origPass")
                .nom("OrigNom")
                .prenom("OrigPrenom")
                .build();

        AdministrateurResponse respNoPwd = AdministrateurResponse.builder()
                .idAdmin(4)
                .email("userNoPwd")
                .username("userNoPwd")
                .nom("NomNoPwd")
                .prenom("PrenomNoPwd")
                .disponibilites(Collections.emptyList())
                .build();

        when(repository.findById(4)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toResponse(existing)).thenReturn(respNoPwd);

        Optional<AdministrateurResponse> result = service.update(4, updateReq);

        assertTrue(result.isPresent());
        assertEquals(respNoPwd, result.get());
        verify(repository).findById(4);
        verify(passwordEncoder, never()).encode(anyString());
        verify(repository).save(existing);
        verify(mapper).toResponse(existing);
    }

    @Test
    void testUpdate_WhenNotExists() {
        when(repository.findById(5)).thenReturn(Optional.empty());

        Optional<AdministrateurResponse> result = service.update(5, request);

        assertFalse(result.isPresent());
        verify(repository).findById(5);
        verifyNoMoreInteractions(repository, mapper, passwordEncoder);
    }

    @Test
    void testDelete_WhenExists() {
        when(repository.existsById(6)).thenReturn(true);

        boolean result = service.delete(6);

        assertTrue(result);
        verify(repository).existsById(6);
        verify(repository).deleteById(6);
    }

    @Test
    void testDelete_WhenNotExists() {
        when(repository.existsById(7)).thenReturn(false);

        boolean result = service.delete(7);

        assertFalse(result);
        verify(repository).existsById(7);
        verify(repository, never()).deleteById(anyInt());
    }
}
