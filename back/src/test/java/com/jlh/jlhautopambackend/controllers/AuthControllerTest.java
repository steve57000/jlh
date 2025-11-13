package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.controllers.AuthController.LoginRequest;
import com.jlh.jlhautopambackend.utils.JwtUtil;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
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
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

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
    @DisplayName("POST /api/auth/login ➔ 401, identifiants invalides")
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
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Identifiants invalides"));
    }
}
