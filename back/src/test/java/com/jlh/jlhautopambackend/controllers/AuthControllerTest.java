package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.controllers.AuthController.LoginRequest;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import com.jlh.jlhautopambackend.services.ClientService;
import com.jlh.jlhautopambackend.services.EmailVerificationService;
import com.jlh.jlhautopambackend.services.PasswordResetService;
import com.jlh.jlhautopambackend.utils.JwtUtil;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Qualifier;
import com.jlh.jlhautopambackend.config.JwtAuthenticationFilter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
@Import(AuthControllerTest.MockBeans.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ClientService clientService;

    @MockitoBean
    private ClientRepository clientRepository;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @TestConfiguration
    static class MockBeans {
        @Bean
        @Qualifier("userService")
        UserDetailsService userDetailsService() {
            return Mockito.mock(UserDetailsService.class);
        }
    }

    @Test
    @DisplayName("POST /api/auth/login ➔ 200, retourne un token JWT")
    void testLoginSuccess() throws Exception {
        // Prépare la requête
        LoginRequest req = new LoginRequest();
        req.setEmail("user");
        req.setPassword("pass");

        // Stub de l'AuthenticationManager pour simuler un login réussi
        Authentication auth = Mockito.mock(Authentication.class);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("user")
                .password("pass")
                .roles("ADMIN")
                .build();

        Mockito.when(authManager.authenticate(
                Mockito.any(UsernamePasswordAuthenticationToken.class))
        ).thenReturn(auth);

        // Stub pour que auth.getPrincipal() retourne le UserDetails simulé
        Mockito.when(auth.getPrincipal()).thenReturn(userDetails);

        // Stub repository pour vérifier l'état de vérification
        Mockito.when(clientRepository.findByEmailIgnoreCase("user")).thenReturn(Optional.of(
                Client.builder().email("user").emailVerified(true).idClient(42).build()
        ));

        // Stub du JwtUtil pour renvoyer un token fixe (pour un UserDetails)
        Mockito.when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");

        // Exécution et assertions
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    @DisplayName("POST /api/auth/login ➔ 403, identifiants invalides")
    void testLoginFailure() throws Exception {
        // Prépare la requête
        LoginRequest req = new LoginRequest();
        req.setEmail("bad");
        req.setPassword("credentials");

        // Stub pour lever une exception d'authentification
        Mockito.when(authManager.authenticate(
                Mockito.any(UsernamePasswordAuthenticationToken.class))
        ).thenThrow(new BadCredentialsException("Bad credentials"));

        // Exécution et assertions
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Bad credentials"));
    }
}
