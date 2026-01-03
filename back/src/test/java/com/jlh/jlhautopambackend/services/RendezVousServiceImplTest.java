package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.RendezVousRequest;
import com.jlh.jlhautopambackend.dto.RendezVousResponse;
import com.jlh.jlhautopambackend.dto.StatutRendezVousDto;
import com.jlh.jlhautopambackend.mapper.RendezVousMapper;
import com.jlh.jlhautopambackend.modeles.*;
import com.jlh.jlhautopambackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RendezVousServiceImplTest {

    @Mock private RendezVousRepository repo;
    @Mock private DemandeRepository demandeRepo;
    @Mock private CreneauRepository creneauRepo;
    @Mock private AdministrateurRepository adminRepo;
    @Mock private StatutRendezVousRepository statutRepo;
    @Mock private StatutDemandeRepository statutDemandeRepo;
    @Mock private TypeDemandeRepository typeDemandeRepo;
    @Mock private RendezVousMapper mapper;
    @Mock private DemandeTimelineService timelineService;

    @InjectMocks
    private RendezVousServiceImpl service;

    private RendezVousRequest request;
    private Demande demande;
    private Creneau creneau;
    private Administrateur admin;
    private StatutRendezVous statut;
    private RendezVous entityWithoutRel;
    private RendezVous savedEntity;
    private RendezVousResponse response;

    @BeforeEach
    void setUp() {
        request = RendezVousRequest.builder()
                .demandeId(1)
                .creneauId(2)
                .administrateurId(3)
                .codeStatut("ST1")
                .build();

        demande = Demande.builder()
                .idDemande(1)
                .statutDemande(StatutDemande.builder().codeStatut("Brouillon").build())
                .build();
        creneau = Creneau.builder().idCreneau(2).build();
        admin = Administrateur.builder()
                .idAdmin(3)
                .username("admin3")
                .email("admin@example.com")
                .build();
        statut = StatutRendezVous.builder()
                .codeStatut("ST1")
                .libelle("Planned")
                .build();

        entityWithoutRel = RendezVous.builder().build();
        savedEntity = RendezVous.builder()
                .idRdv(10)
                .demande(demande)
                .creneau(creneau)
                .administrateur(admin)
                .statut(statut)
                .build();

        response = RendezVousResponse.builder()
                .idRdv(10)
                .demandeId(1)
                .creneauId(2)
                .administrateurId(3)
                .statut(new StatutRendezVousDto("ST1","Planned"))
                .build();
    }

    @Test
    void testFindAll_ShouldReturnAll() {
        RendezVous other = RendezVous.builder()
                .idRdv(11)
                .demande(demande)
                .creneau(creneau)
                .administrateur(admin)
                .statut(statut)
                .build();
        RendezVousResponse otherResp = RendezVousResponse.builder()
                .idRdv(11)
                .demandeId(1)
                .creneauId(2)
                .administrateurId(3)
                .statut(new StatutRendezVousDto("ST1","Planned"))
                .build();

        when(repo.findAll()).thenReturn(Arrays.asList(savedEntity, other));
        when(mapper.toResponse(savedEntity)).thenReturn(response);
        when(mapper.toResponse(other)).thenReturn(otherResp);

        List<RendezVousResponse> results = service.findAll();

        assertEquals(2, results.size());
        assertEquals(response, results.get(0));
        assertEquals(otherResp, results.get(1));
        verify(repo).findAll();
    }

    @Test
    void testFindById_WhenFound() {
        when(repo.findById(10)).thenReturn(Optional.of(savedEntity));
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        Optional<RendezVousResponse> result = service.findById(10);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
        verify(repo).findById(10);
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    void testFindById_WhenNotFound() {
        when(repo.findById(99)).thenReturn(Optional.empty());

        Optional<RendezVousResponse> result = service.findById(99);

        assertFalse(result.isPresent());
        verify(repo).findById(99);
    }

    @Test
    void testCreate_ShouldThrowWhenDemandeNotFound() {
        when(demandeRepo.findById(1)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(request)
        );
        assertEquals("Demande introuvable: 1", ex.getMessage());
        verify(demandeRepo).findById(1);
        verifyNoMoreInteractions(creneauRepo, adminRepo, statutRepo, repo, typeDemandeRepo, statutDemandeRepo, timelineService);
    }

    @Test
    void testCreate_ShouldThrowWhenCreneauNotFound() {
        TypeDemande typeRdv = TypeDemande.builder().codeType("RendezVous").build();
        when(demandeRepo.findById(1)).thenReturn(Optional.of(demande));
        when(creneauRepo.findById(2)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(request)
        );
        assertEquals("Creneau introuvable: 2", ex.getMessage());
        verify(creneauRepo).findById(2);
    }

    @Test
    void testCreate_ShouldThrowWhenAdminNotFound() {
        when(demandeRepo.findById(1)).thenReturn(Optional.of(demande));
        when(creneauRepo.findById(2)).thenReturn(Optional.of(creneau));
        when(adminRepo.findById(3)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(request)
        );
        assertEquals("Administrateur introuvable: 3", ex.getMessage());
        verify(adminRepo).findById(3);
    }

    @Test
    void testCreate_ShouldThrowWhenStatutNotFound() {
        when(demandeRepo.findById(1)).thenReturn(Optional.of(demande));
        when(creneauRepo.findById(2)).thenReturn(Optional.of(creneau));
        when(adminRepo.findById(3)).thenReturn(Optional.of(admin));
        when(statutRepo.findById("ST1")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(request)
        );
        assertEquals("Statut RDV introuvable: ST1", ex.getMessage());
        verify(statutRepo).findById("ST1");
    }

    @Test
    void testCreate_ShouldSetRelationsAndReturnResponse() {
        TypeDemande typeRdv = TypeDemande.builder().codeType("RendezVous").build();
        StatutDemande enAttente = StatutDemande.builder().codeStatut("En_attente").libelle("En attente").build();
        when(demandeRepo.findById(1)).thenReturn(Optional.of(demande));
        when(typeDemandeRepo.findById("RendezVous")).thenReturn(Optional.of(typeRdv));
        when(demandeRepo.save(demande)).thenReturn(demande);
        when(creneauRepo.findById(2)).thenReturn(Optional.of(creneau));
        when(adminRepo.findById(3)).thenReturn(Optional.of(admin));
        when(statutRepo.findById("ST1")).thenReturn(Optional.of(statut));
        when(statutDemandeRepo.findById("En_attente")).thenReturn(Optional.of(enAttente));
        when(mapper.toEntity(request)).thenReturn(entityWithoutRel);
        when(repo.save(entityWithoutRel)).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        RendezVousResponse result = service.create(request);

        assertEquals(response, result);
        ArgumentCaptor<RendezVous> captor = ArgumentCaptor.forClass(RendezVous.class);
        verify(repo).save(captor.capture());
        RendezVous passed = captor.getValue();
        assertEquals(demande, passed.getDemande());
        assertEquals(creneau, passed.getCreneau());
        assertEquals(admin, passed.getAdministrateur());
        assertEquals(statut, passed.getStatut());
        verify(timelineService).logRendezVousEvent(demande, savedEntity, "Rendez-vous planifié", admin.getEmail(), "ADMIN");
        verify(timelineService).logStatusChange(demande, enAttente, "Brouillon", admin.getEmail(), "ADMIN");
    }

    @Test
    void testUpdate_WhenNotExists() {
        when(repo.findById(10)).thenReturn(Optional.empty());

        Optional<RendezVousResponse> result = service.update(10, request);

        assertFalse(result.isPresent());
        verify(repo).findById(10);
    }

    @Test
    void testUpdate_ShouldThrowWhenRelatedNotFound() {
        when(repo.findById(10)).thenReturn(Optional.of(savedEntity));
        when(demandeRepo.findById(1)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(10, request)
        );
        assertEquals("Demande introuvable: 1", ex.getMessage());
    }

    @Test
    void testUpdate_WhenExists_ShouldSaveAndReturn() {
        RendezVous updated = RendezVous.builder()
                .idRdv(10)
                .demande(demande)
                .creneau(creneau)
                .administrateur(admin)
                .statut(statut)
                .build();
        RendezVousResponse updatedResp = RendezVousResponse.builder()
                .idRdv(10)
                .demandeId(1)
                .creneauId(2)
                .administrateurId(3)
                .statut(new StatutRendezVousDto("ST1","Planned"))
                .build();

        when(repo.findById(10)).thenReturn(Optional.of(savedEntity));
        when(demandeRepo.findById(1)).thenReturn(Optional.of(demande));
        when(creneauRepo.findById(2)).thenReturn(Optional.of(creneau));
        when(adminRepo.findById(3)).thenReturn(Optional.of(admin));
        when(statutRepo.findById("ST1")).thenReturn(Optional.of(statut));
        when(repo.save(savedEntity)).thenReturn(updated);
        when(mapper.toResponse(updated)).thenReturn(updatedResp);

        Optional<RendezVousResponse> result = service.update(10, request);

        assertTrue(result.isPresent());
        assertEquals(updatedResp, result.get());
        verify(repo).save(savedEntity);
        verify(timelineService).logRendezVousEvent(demande, updated, "Rendez-vous mis à jour", admin.getEmail(), "ADMIN");
    }

    @Test
    void testDelete_WhenExists() {
        when(repo.existsById(10)).thenReturn(true);

        boolean result = service.delete(10);

        assertTrue(result);
        verify(repo).existsById(10);
        verify(repo).deleteById(10);
    }

    @Test
    void testDelete_WhenNotExists() {
        when(repo.existsById(11)).thenReturn(false);

        boolean result = service.delete(11);

        assertFalse(result);
        verify(repo).existsById(11);
        verify(repo, never()).deleteById(anyInt());
    }
}
