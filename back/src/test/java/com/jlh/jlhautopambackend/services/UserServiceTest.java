package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.repository.AdministrateurRepository;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AdministrateurRepository adminRepo;

    @Mock
    private ClientRepository clientRepo;

    @InjectMocks
    private UserService service;  // on teste maintenant UserService

    @Test
    void loadUserByUsername_whenAdminExists_returnsAdminDetails() {
        // Préparation du mock Admin
        Administrateur admin = new Administrateur();
        admin.setEmail("admin@example.com");
        admin.setMotDePasse("encodedPassword");
        when(adminRepo.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(admin));
        // on ne stub pas clientRepo, car on tombe sur la branche admin

        // Exécution
        UserDetails userDetails = service.loadUserByUsername("admin@example.com");

        // Vérifications
        assertEquals("admin@example.com", userDetails.getUsername());
        assertEquals("encodedPassword",   userDetails.getPassword());
        assertTrue(
                userDetails.getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")),
                "L'autorité ADMIN doit être présente"
        );
    }

    @Test
    void loadUserByUsername_whenNeitherAdminNorClient_throwsException() {
        when(adminRepo.findByEmail("nobody@example.com"))
                .thenReturn(Optional.empty());
        when(clientRepo.findByEmailIgnoreCase("nobody@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("nobody@example.com")
        );
    }
}
