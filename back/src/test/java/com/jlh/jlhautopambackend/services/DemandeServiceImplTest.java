package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.DemandeRequest;
import com.jlh.jlhautopambackend.dto.DemandeResponse;
import com.jlh.jlhautopambackend.dto.StatutDemandeDto;
import com.jlh.jlhautopambackend.dto.TypeDemandeDto;
import com.jlh.jlhautopambackend.mapper.DemandeMapper;
import com.jlh.jlhautopambackend.modeles.*;
import com.jlh.jlhautopambackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemandeServiceImplTest {

    @Mock
    private DemandeRepository repository;
    @Mock
    private ClientRepository clientRepo;
    @Mock
    private TypeDemandeRepository typeRepo;
    @Mock
    private StatutDemandeRepository statutRepo;
    @Mock
    private DemandeMapper mapper;

    @InjectMocks
    private DemandeServiceImpl service;

    private DemandeRequest request;
    private Demande entity;
    private Demande savedEntity;
    private DemandeResponse response;
    private Client client;
    private TypeDemande type;
    private StatutDemande statut;
    private Instant date;

    @BeforeEach
    void setUp() {
        date = Instant.parse("2025-07-14T09:00:00Z");
        request = DemandeRequest.builder()
                .dateDemande(date)
                .clientId(100)
                .codeType("TYPE1")
                .codeStatut("STAT1")
                .build();

        entity = Demande.builder()
                .dateDemande(date)
                .build();

        client = Client.builder()
                .idClient(100)
                .nom("Doe")
                .prenom("John")
                .email("john@example.com")
                .telephone("0123456789")
                .adresseLigne1("1 rue A")
                .adresseLigne2("2 rue B")
                .adresseVille("Metz")
                .adresseCodePostal("12345")
                .build();

        type = TypeDemande.builder()
                .codeType("TYPE1")
                .libelle("Type One")
                .build();

        statut = StatutDemande.builder()
                .codeStatut("STAT1")
                .libelle("Statut One")
                .build();

        savedEntity = Demande.builder()
                .idDemande(1)
                .dateDemande(date)
                .client(client)
                .typeDemande(type)
                .statutDemande(statut)
                .services(Collections.emptyList())
                .build();

        response = DemandeResponse.builder()
                .idDemande(1)
                .dateDemande(date)
                .typeDemande(new TypeDemandeDto(type.getCodeType(), type.getLibelle()))
                .statutDemande(new StatutDemandeDto(statut.getCodeStatut(), statut.getLibelle()))
                .services(Collections.emptyList())
                .build();
    }

    @Test
    void testCreate_ShouldSetRelationsAndReturnResponse() {
        when(mapper.toEntity(request)).thenReturn(entity);
        when(clientRepo.findById(100)).thenReturn(Optional.of(client));
        when(typeRepo.findById("TYPE1")).thenReturn(Optional.of(type));
        when(statutRepo.findById("STAT1")).thenReturn(Optional.of(statut));
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        DemandeResponse result = service.create(request);

        assertEquals(response, result);
        ArgumentCaptor<Demande> captor = ArgumentCaptor.forClass(Demande.class);
        verify(mapper).toEntity(request);
        verify(clientRepo).findById(100);
        verify(typeRepo).findById("TYPE1");
        verify(statutRepo).findById("STAT1");
        verify(repository).save(captor.capture());
        Demande passed = captor.getValue();
        assertEquals(client, passed.getClient());
        assertEquals(type, passed.getTypeDemande());
        assertEquals(statut, passed.getStatutDemande());
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    void testCreate_ShouldThrowWhenClientNotFound() {
        when(mapper.toEntity(request)).thenReturn(entity);
        when(clientRepo.findById(100)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(request));
        assertEquals("Client introuvable", ex.getMessage());
        verify(clientRepo).findById(100);
        verifyNoMoreInteractions(typeRepo, statutRepo, repository, mapper);
    }

    @Test
    void testCreate_ShouldThrowWhenTypeNotFound() {
        when(mapper.toEntity(request)).thenReturn(entity);
        when(clientRepo.findById(100)).thenReturn(Optional.of(client));
        when(typeRepo.findById("TYPE1")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(request));
        assertEquals("Type introuvable", ex.getMessage());
        verify(clientRepo).findById(100);
        verify(typeRepo).findById("TYPE1");
        verifyNoMoreInteractions(statutRepo, repository, mapper);
    }

    @Test
    void testCreate_ShouldThrowWhenStatutNotFound() {
        when(mapper.toEntity(request)).thenReturn(entity);
        when(clientRepo.findById(100)).thenReturn(Optional.of(client));
        when(typeRepo.findById("TYPE1")).thenReturn(Optional.of(type));
        when(statutRepo.findById("STAT1")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(request));
        assertEquals("Statut introuvable", ex.getMessage());
        verify(clientRepo).findById(100);
        verify(typeRepo).findById("TYPE1");
        verify(statutRepo).findById("STAT1");
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    void testFindById_WhenFound() {
        when(repository.findById(1)).thenReturn(Optional.of(savedEntity));
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        Optional<DemandeResponse> result = service.findById(1);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
        verify(repository).findById(1);
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    void testFindById_WhenNotFound() {
        when(repository.findById(2)).thenReturn(Optional.empty());

        Optional<DemandeResponse> result = service.findById(2);

        assertFalse(result.isPresent());
        verify(repository).findById(2);
        verifyNoInteractions(mapper);
    }

    @Test
    void testFindAll_ShouldReturnResponses() {
        Demande other = Demande.builder()
                .idDemande(2)
                .dateDemande(date)
                .client(client)
                .typeDemande(type)
                .statutDemande(statut)
                .services(Collections.emptyList())
                .build();
        DemandeResponse otherResp = DemandeResponse.builder()
                .idDemande(2)
                .dateDemande(date)
                .typeDemande(new TypeDemandeDto(type.getCodeType(), type.getLibelle()))
                .statutDemande(new StatutDemandeDto(statut.getCodeStatut(), statut.getLibelle()))
                .services(Collections.emptyList())
                .build();

        when(repository.findAll()).thenReturn(Arrays.asList(savedEntity, other));
        when(mapper.toResponse(savedEntity)).thenReturn(response);
        when(mapper.toResponse(other)).thenReturn(otherResp);

        List<DemandeResponse> results = service.findAll();

        assertEquals(2, results.size());
        assertEquals(response, results.get(0));
        assertEquals(otherResp, results.get(1));
        verify(repository).findAll();
        verify(mapper).toResponse(savedEntity);
        verify(mapper).toResponse(other);
    }

    @Test
    void testUpdate_WhenExists_ShouldUpdateAndReturnResponse() {
        DemandeRequest updateReq = DemandeRequest.builder()
                .dateDemande(date.plusSeconds(3600))
                .clientId(200)
                .codeType("TYPE2")
                .codeStatut("STAT2")
                .build();
        Client newClient = Client.builder().idClient(200).build();
        TypeDemande newType = TypeDemande.builder().codeType("TYPE2").build();
        StatutDemande newStatut = StatutDemande.builder().codeStatut("STAT2").build();
        Demande existing = savedEntity;
        Demande updatedEntity = Demande.builder()
                .idDemande(1)
                .dateDemande(updateReq.getDateDemande())
                .client(newClient)
                .typeDemande(newType)
                .statutDemande(newStatut)
                .services(Collections.emptyList())
                .build();
        DemandeResponse updatedResp = DemandeResponse.builder()
                .idDemande(1)
                .dateDemande(updateReq.getDateDemande())
                .typeDemande(new TypeDemandeDto(newType.getCodeType(), null))
                .statutDemande(new StatutDemandeDto(newStatut.getCodeStatut(), null))
                .services(Collections.emptyList())
                .build();

        when(repository.findById(1)).thenReturn(Optional.of(existing));
        when(clientRepo.findById(200)).thenReturn(Optional.of(newClient));
        when(typeRepo.findById("TYPE2")).thenReturn(Optional.of(newType));
        when(statutRepo.findById("STAT2")).thenReturn(Optional.of(newStatut));
        when(repository.save(existing)).thenReturn(updatedEntity);
        when(mapper.toResponse(updatedEntity)).thenReturn(updatedResp);

        Optional<DemandeResponse> result = service.update(1, updateReq);

        assertTrue(result.isPresent());
        assertEquals(updatedResp, result.get());
        verify(repository).findById(1);
        verify(clientRepo).findById(200);
        verify(typeRepo).findById("TYPE2");
        verify(statutRepo).findById("STAT2");
        verify(repository).save(existing);
        verify(mapper).toResponse(updatedEntity);
    }

    @Test
    void testUpdate_WhenNotExists() {
        when(repository.findById(3)).thenReturn(Optional.empty());

        Optional<DemandeResponse> result = service.update(3, request);

        assertFalse(result.isPresent());
        verify(repository).findById(3);
        verifyNoMoreInteractions(clientRepo, typeRepo, statutRepo, mapper);
    }

    @Test
    void testDelete_WhenExists() {
        when(repository.existsById(4)).thenReturn(true);

        boolean result = service.delete(4);

        assertTrue(result);
        verify(repository).existsById(4);
        verify(repository).deleteById(4);
    }

    @Test
    void testDelete_WhenNotExists() {
        when(repository.existsById(5)).thenReturn(false);

        boolean result = service.delete(5);

        assertFalse(result);
        verify(repository).existsById(5);
        verify(repository, never()).deleteById(anyInt());
    }
}
