package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.modeles.Creneau;
import com.jlh.jlhautopambackend.modeles.Disponibilite;
import com.jlh.jlhautopambackend.modeles.DisponibiliteKey;
import com.jlh.jlhautopambackend.repository.AdministrateurRepository;
import com.jlh.jlhautopambackend.repository.CreneauRepository;
import com.jlh.jlhautopambackend.repository.DisponibiliteRepository;
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
class DisponibiliteServiceImplTest {

    @Mock
    private DisponibiliteRepository dispoRepo;
    @Mock
    private AdministrateurRepository adminRepo;
    @Mock
    private CreneauRepository creneauRepo;

    @InjectMocks
    private DisponibiliteServiceImpl service;

    private Administrateur adminStub;
    private Creneau creneauStub;
    private DisponibiliteKey key;
    private Disponibilite savedEntity;
    private Disponibilite dto;

    @BeforeEach
    void setUp() {
        // Prepare stubbed admin and creneau
        adminStub = Administrateur.builder()
                .idAdmin(10)
                .username("admin10")
                .build();
        creneauStub = Creneau.builder()
                .idCreneau(20)
                .build();
        // The DTO passed into create()
        dto = Disponibilite.builder()
                .administrateur(adminStub)
                .creneau(creneauStub)
                .build();
        key = new DisponibiliteKey(adminStub.getIdAdmin(), creneauStub.getIdCreneau());
        // The entity returned by save()
        savedEntity = Disponibilite.builder()
                .id(key)
                .administrateur(adminStub)
                .creneau(creneauStub)
                .build();
    }

    @Test
    void testFindAll_ShouldReturnAllDisponibilites() {
        Disponibilite other = Disponibilite.builder()
                .id(new DisponibiliteKey(11,21))
                .administrateur(Administrateur.builder()
                        .idAdmin(11)
                        .username("admin11")
                        .build())
                .creneau(Creneau.builder().idCreneau(21).build())
                .build();
        when(dispoRepo.findAll()).thenReturn(Arrays.asList(savedEntity, other));

        List<Disponibilite> results = service.findAll();

        assertEquals(2, results.size());
        assertTrue(results.contains(savedEntity));
        assertTrue(results.contains(other));
        verify(dispoRepo).findAll();
    }

    @Test
    void testFindByKey_WhenFound() {
        when(dispoRepo.findById(key)).thenReturn(Optional.of(savedEntity));

        Optional<Disponibilite> result = service.findByKey(adminStub.getIdAdmin(), creneauStub.getIdCreneau());

        assertTrue(result.isPresent());
        assertEquals(savedEntity, result.get());
        verify(dispoRepo).findById(key);
    }

    @Test
    void testFindByKey_WhenNotFound() {
        when(dispoRepo.findById(key)).thenReturn(Optional.empty());

        Optional<Disponibilite> result = service.findByKey(adminStub.getIdAdmin(), creneauStub.getIdCreneau());

        assertFalse(result.isPresent());
        verify(dispoRepo).findById(key);
    }

    @Test
    void testCreate_ShouldThrowWhenAdminNotFound() {
        when(adminRepo.findById(adminStub.getIdAdmin())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(dto));
        assertEquals("Administrateur introuvable", ex.getMessage());
        verify(adminRepo).findById(adminStub.getIdAdmin());
        verifyNoMoreInteractions(creneauRepo, dispoRepo);
    }

    @Test
    void testCreate_ShouldThrowWhenCreneauNotFound() {
        when(adminRepo.findById(adminStub.getIdAdmin())).thenReturn(Optional.of(adminStub));
        when(creneauRepo.findById(creneauStub.getIdCreneau())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(dto));
        assertEquals("Créneau introuvable", ex.getMessage());
        verify(adminRepo).findById(adminStub.getIdAdmin());
        verify(creneauRepo).findById(creneauStub.getIdCreneau());
        verifyNoMoreInteractions(dispoRepo);
    }

    @Test
    void testCreate_ShouldSaveAndReturnNewEntity() {
        when(adminRepo.findById(adminStub.getIdAdmin())).thenReturn(Optional.of(adminStub));
        when(creneauRepo.findById(creneauStub.getIdCreneau())).thenReturn(Optional.of(creneauStub));
        when(dispoRepo.save(any(Disponibilite.class))).thenReturn(savedEntity);

        Disponibilite result = service.create(dto);

        assertEquals(savedEntity, result);
        ArgumentCaptor<Disponibilite> captor = ArgumentCaptor.forClass(Disponibilite.class);
        verify(adminRepo).findById(adminStub.getIdAdmin());
        verify(creneauRepo).findById(creneauStub.getIdCreneau());
        verify(dispoRepo).save(captor.capture());
        Disponibilite passed = captor.getValue();
        assertEquals(key, passed.getId());
        assertEquals(adminStub, passed.getAdministrateur());
        assertEquals(creneauStub, passed.getCreneau());
    }

    @Test
    void testDelete_WhenExists() {
        when(dispoRepo.existsById(key)).thenReturn(true);

        boolean result = service.delete(adminStub.getIdAdmin(), creneauStub.getIdCreneau());

        assertTrue(result);
        verify(dispoRepo).existsById(key);
        verify(dispoRepo).deleteById(key);
    }

    @Test
    void testDelete_WhenNotExists() {
        when(dispoRepo.existsById(key)).thenReturn(false);

        boolean result = service.delete(adminStub.getIdAdmin(), creneauStub.getIdCreneau());

        assertFalse(result);
        verify(dispoRepo).existsById(key);
        verify(dispoRepo, never()).deleteById(any());
    }
}
