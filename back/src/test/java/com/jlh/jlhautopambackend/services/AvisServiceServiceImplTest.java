package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.AvisServiceRequest;
import com.jlh.jlhautopambackend.dto.AvisServiceResponse;
import com.jlh.jlhautopambackend.mapper.AvisServiceMapper;
import com.jlh.jlhautopambackend.modeles.AvisService;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.DemandeService;
import com.jlh.jlhautopambackend.modeles.Service;
import com.jlh.jlhautopambackend.modeles.StatutDemande;
import com.jlh.jlhautopambackend.repository.AvisServiceRepository;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import com.jlh.jlhautopambackend.repository.DemandeServiceRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvisServiceServiceImplTest {

    @Mock
    private AvisServiceRepository avisRepository;
    @Mock
    private DemandeRepository demandeRepository;
    @Mock
    private DemandeServiceRepository demandeServiceRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private AvisServiceMapper mapper;

    @InjectMocks
    private AvisServiceServiceImpl service;

    @Test
    void create_ShouldUseRequestedServiceFromDemande() {
        Integer clientId = 10;
        Integer demandeId = 20;
        Integer selectedServiceId = 2;

        Client client = Client.builder().idClient(clientId).build();
        Demande demande = Demande.builder()
                .idDemande(demandeId)
                .client(client)
                .statutDemande(StatutDemande.builder().codeStatut("Traitee").build())
                .build();

        Service service1 = Service.builder().idService(1).libelle("Service 1").build();
        Service service2 = Service.builder().idService(2).libelle("Service 2").build();

        AvisServiceRequest request = AvisServiceRequest.builder()
                .demandeId(demandeId)
                .serviceId(selectedServiceId)
                .note(5)
                .commentaire("Super")
                .build();

        when(demandeRepository.findById(demandeId)).thenReturn(Optional.of(demande));
        when(demandeServiceRepository.findByDemande_IdDemande(demandeId)).thenReturn(List.of(
                DemandeService.builder().demande(demande).service(service1).build(),
                DemandeService.builder().demande(demande).service(service2).build()
        ));
        when(avisRepository.existsByDemande_IdDemandeAndService_IdService(demandeId, selectedServiceId)).thenReturn(false);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        AvisService saved = AvisService.builder().idAvis(99L).demande(demande).service(service2).client(client).build();
        AvisServiceResponse response = AvisServiceResponse.builder().idAvis(99L).serviceId(selectedServiceId).build();
        when(avisRepository.save(any(AvisService.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        AvisServiceResponse result = service.create(clientId, request);

        assertEquals(99L, result.getIdAvis());
        ArgumentCaptor<AvisService> avisCaptor = ArgumentCaptor.forClass(AvisService.class);
        verify(avisRepository).save(avisCaptor.capture());
        assertEquals(selectedServiceId, avisCaptor.getValue().getService().getIdService());
    }

    @Test
    void create_ShouldRejectServiceNotAttachedToDemande() {
        Integer clientId = 10;
        Integer demandeId = 20;

        Client client = Client.builder().idClient(clientId).build();
        Demande demande = Demande.builder()
                .idDemande(demandeId)
                .client(client)
                .statutDemande(StatutDemande.builder().codeStatut("Traitee").build())
                .build();

        AvisServiceRequest request = AvisServiceRequest.builder()
                .demandeId(demandeId)
                .serviceId(999)
                .note(4)
                .commentaire("Bien")
                .build();

        when(demandeRepository.findById(demandeId)).thenReturn(Optional.of(demande));
        when(demandeServiceRepository.findByDemande_IdDemande(demandeId)).thenReturn(List.of(
                DemandeService.builder().demande(demande).service(Service.builder().idService(1).build()).build()
        ));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.create(clientId, request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("409 CONFLICT \"Le service indiqué n'est pas associé à cette demande.\"", ex.getMessage());
    }
}
