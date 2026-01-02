package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ClientRequest;
import com.jlh.jlhautopambackend.dto.ClientResponse;
import com.jlh.jlhautopambackend.mapper.ClientMapper;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository repository;

    @Mock
    private ClientMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private ClientServiceImpl service;

    private ClientRequest request;
    private Client entity;
    private Client savedEntity;
    private ClientResponse response;

    @BeforeEach
    void setUp() {
        request = ClientRequest.builder()
                .nom("Doe")
                .prenom("John")
                .email("john.doe@example.com")
                .motDePasse("secret123")
                .immatriculation("AB-123-CD")
                .vehiculeMarque("Peugeot")
                .vehiculeModele("308")
                .telephone("0123456789")
                .adresseLigne1("I")
                .adresseLigne2("J")
                .codePostal("57")
                .ville("Metz")
                .build();

        entity = Client.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .immatriculation(request.getImmatriculation())
                .vehiculeMarque(request.getVehiculeMarque())
                .vehiculeModele(request.getVehiculeModele())
                .telephone(request.getTelephone())
                .adresseLigne1(request.getAdresseLigne1())
                .adresseLigne2(request.getAdresseLigne2())
                .adresseVille(request.getVille())
                .adresseCodePostal(request.getCodePostal())
                .motDePasse("ENC(secret123)")
                .build();

        savedEntity = Client.builder()
                .idClient(1)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .immatriculation(request.getImmatriculation())
                .vehiculeMarque(request.getVehiculeMarque())
                .vehiculeModele(request.getVehiculeModele())
                .telephone(request.getTelephone())
                .adresseLigne1(request.getAdresseLigne1())
                .adresseLigne2(request.getAdresseLigne2())
                .adresseVille(request.getVille())
                .adresseCodePostal(request.getCodePostal())
                .motDePasse("ENC(secret123)")
                .build();

        response = ClientResponse.builder()
                .idClient(savedEntity.getIdClient())
                .nom(savedEntity.getNom())
                .prenom(savedEntity.getPrenom())
                .email(savedEntity.getEmail())
                .immatriculation(savedEntity.getImmatriculation())
                .vehiculeMarque(savedEntity.getVehiculeMarque())
                .vehiculeModele(savedEntity.getVehiculeModele())
                .telephone(savedEntity.getTelephone())
                .adresseLigne1(savedEntity.getAdresseLigne1())
                .adresseLigne2(savedEntity.getAdresseLigne2())
                .ville(savedEntity.getAdresseVille())
                .codePostal(savedEntity.getAdresseCodePostal())
                .build();

    }

    @Test
    void testCreate_ShouldSaveAndReturnResponse() {
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        ClientResponse result = service.create(request);

        assertEquals(response, result);
        verify(mapper).toEntity(request);
        verify(repository).save(entity);
        verify(mapper).toResponse(savedEntity);
        verify(emailVerificationService).sendVerificationForClient(savedEntity.getIdClient());
    }

    @Test
    void testFindById_WhenFound() {
        when(repository.findById(1)).thenReturn(Optional.of(savedEntity));
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        Optional<ClientResponse> result = service.findById(1);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
        verify(repository).findById(1);
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    void testFindById_WhenNotFound() {
        when(repository.findById(2)).thenReturn(Optional.empty());

        Optional<ClientResponse> result = service.findById(2);

        assertFalse(result.isPresent());
        verify(repository).findById(2);
        verifyNoInteractions(mapper);
    }

    @Test
    void testFindAll_ShouldReturnListOfResponses() {
        Client other = Client.builder()
                .idClient(2)
                .nom("Smith")
                .prenom("Jane")
                .email("jane.smith@example.com")
                .immatriculation("CD-456-EF")
                .vehiculeMarque("Renault")
                .vehiculeModele("Clio")
                .telephone("0987654321")
                .adresseLigne1("1 rue dg")
                .adresseLigne2("2 rue dg")
                .adresseVille("Metz")
                .adresseCodePostal("57")
                .build();
        ClientResponse otherResp = ClientResponse.builder()
                .idClient(2)
                .nom("Smith")
                .prenom("Jane")
                .email("jane.smith@example.com")
                .immatriculation("CD-456-EF")
                .vehiculeMarque("Renault")
                .vehiculeModele("Clio")
                .telephone("0987654321")
                .adresseLigne1("1 rue dg")
                .adresseLigne2("2 rue dg")
                .ville("Metz")
                .codePostal("57")
                .build();

        when(repository.findAll()).thenReturn(Arrays.asList(savedEntity, other));
        when(mapper.toResponse(savedEntity)).thenReturn(response);
        when(mapper.toResponse(other)).thenReturn(otherResp);

        List<ClientResponse> results = service.findAll();

        assertEquals(2, results.size());
        assertEquals(response, results.get(0));
        assertEquals(otherResp, results.get(1));
        verify(repository).findAll();
        verify(mapper).toResponse(savedEntity);
        verify(mapper).toResponse(other);
    }

    @Test
    void testUpdate_WhenExists() {
        ClientRequest updateReq = ClientRequest.builder()
                .nom("Updated")
                .prenom("User")
                .email("updated.user@example.com")
                .motDePasse("password999")
                .immatriculation("XY-999-ZZ")
                .vehiculeMarque("Citroen")
                .vehiculeModele("C5 Aircross")
                .telephone("0112233445")
                .adresseLigne1("789 Oak St")
                .adresseLigne2("981 Sur Md")
                .codePostal("57")
                .ville("Metz")
                .build();

        Client existing = Client.builder()
                .idClient(3)
                .nom("Old")
                .prenom("Old")
                .email("old@example.com")
                .immatriculation("OLD-111-XX")
                .vehiculeMarque("Ford")
                .vehiculeModele("Focus")
                .telephone("0000000000")
                .adresseLigne1("Old Addr")
                .adresseLigne2("Old Addr")
                .adresseVille("Old Addr")
                .adresseCodePostal("Old Addr")
                .motDePasse("ENC(secret123)")
                .build();

        Client updatedEntity = Client.builder()
                .idClient(3)
                .nom(updateReq.getNom())
                .prenom(updateReq.getPrenom())
                .email(updateReq.getEmail())
                .immatriculation(updateReq.getImmatriculation())
                .vehiculeMarque(updateReq.getVehiculeMarque())
                .vehiculeModele(updateReq.getVehiculeModele())
                .telephone(updateReq.getTelephone())
                .adresseLigne1(updateReq.getAdresseLigne1())
                .adresseLigne2(updateReq.getAdresseLigne2())
                .adresseCodePostal(updateReq.getCodePostal())
                .adresseVille(updateReq.getVille())
                .motDePasse("ENC(password999)")
                .build();

        ClientResponse updatedResp = ClientResponse.builder()
                .idClient(3)
                .nom(updateReq.getNom())
                .prenom(updateReq.getPrenom())
                .email(updateReq.getEmail())
                .immatriculation(updateReq.getImmatriculation())
                .vehiculeMarque(updateReq.getVehiculeMarque())
                .vehiculeModele(updateReq.getVehiculeModele())
                .telephone(updateReq.getTelephone())
                .adresseLigne1(updateReq.getAdresseLigne1())
                .adresseLigne2(updateReq.getAdresseLigne2())
                .ville(updateReq.getVille())
                .codePostal(updateReq.getCodePostal())
                .build();

        when(repository.findById(3)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(updatedEntity);
        when(mapper.toResponse(updatedEntity)).thenReturn(updatedResp);
        when(passwordEncoder.encode("password999")).thenReturn("ENC(password999)");

        Optional<ClientResponse> result = service.update(3, updateReq);

        assertTrue(result.isPresent());
        assertEquals(updatedResp, result.get());
        verify(repository).findById(3);
        verify(repository).save(existing);
        verify(mapper).toResponse(updatedEntity);
        verify(emailVerificationService).sendVerificationForClient(3);
    }

    @Test
    void testUpdate_WhenNotExists() {
        ClientRequest updateReq = ClientRequest.builder()
                .nom("Updated")
                .prenom("User")
                .email("updated.user@example.com")
                .motDePasse("password999")
                .immatriculation("XY-999-ZZ")
                .vehiculeMarque("Citroen")
                .vehiculeModele("C5 Aircross")
                .telephone("0112233445")
                .adresseLigne1("789 Oak St")
                .adresseLigne2("981 Sur Md")
                .codePostal("57")
                .ville("Metz")
                .build();

        when(repository.findById(42)).thenReturn(Optional.empty());

        Optional<ClientResponse> result = service.update(42, updateReq);

        assertFalse(result.isPresent());
        verify(repository).findById(42);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(mapper);
        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void testDelete_WhenExists() {
        when(repository.existsById(8)).thenReturn(true);

        boolean result = service.delete(8);

        assertTrue(result);
        verify(repository).existsById(8);
        verify(repository).deleteById(8);
    }

    @Test
    void testDelete_WhenNotExists() {
        when(repository.existsById(99)).thenReturn(false);

        boolean result = service.delete(99);

        assertFalse(result);
        verify(repository).existsById(99);
        verify(repository, never()).deleteById(anyInt());
    }
}
