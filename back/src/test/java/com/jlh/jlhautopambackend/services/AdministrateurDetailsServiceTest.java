package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.repository.AdministrateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Collection;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministrateurDetailsServiceTest {

    @Mock
    private AdministrateurRepository repo;

    @InjectMocks
    private AdministrateurDetailsService service;

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenAdminExists() {
        // Arrange
        Administrateur admin = Administrateur.builder()
                .idAdmin(10)
                .username("testAdmin")
                .motDePasse("encodedPass")
                .nom("Nom")
                .prenom("Prenom")
                .build();
        when(repo.findByUsername("testAdmin")).thenReturn(Optional.of(admin));

        // Act
        UserDetails userDetails = service.loadUserByUsername("testAdmin");

        // Assert
        assertNotNull(userDetails);
        assertEquals(admin.getUsername(), userDetails.getUsername());
        assertEquals(admin.getMotDePasse(), userDetails.getPassword());
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        verify(repo).findByUsername("testAdmin");
    }

    @Test
    void loadUserByUsername_ShouldThrow_WhenAdminNotFound() {
        // Arrange
        when(repo.findByUsername("unknown")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("unknown")
        );
        assertEquals("Admin introuvable", ex.getMessage());
        verify(repo).findByUsername("unknown");
    }
}
