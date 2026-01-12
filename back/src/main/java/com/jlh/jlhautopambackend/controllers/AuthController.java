// src/main/java/com/jlh/jlhautopambackend/controllers/AuthController.java
package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.payload.RegisterRequest;
import com.jlh.jlhautopambackend.dto.ClientRequest;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import com.jlh.jlhautopambackend.services.ClientService;
import com.jlh.jlhautopambackend.services.EmailVerificationService;
import com.jlh.jlhautopambackend.dto.ForgotPasswordRequest;
import com.jlh.jlhautopambackend.dto.ResetPasswordRequest;
import com.jlh.jlhautopambackend.services.PasswordResetService;
import com.jlh.jlhautopambackend.utils.JwtUtil;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    private final ClientService clientService;
    private final ClientRepository clientRepo;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest creds) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(creds.getEmail(), creds.getPassword())
            );

            clientRepo.findByEmailIgnoreCase(creds.getEmail()).ifPresent(cli -> {
                if (!cli.isEmailVerified()) {
                    throw new BadCredentialsException("Veuillez vérifier votre e-mail pour activer votre compte.");
                }
            });

            String token = jwtUtil.generateToken((UserDetails) auth.getPrincipal());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(403).body(Map.of("message", ex.getMessage()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body(Map.of("message", "Identifiants invalides"));
        }
    }

    /** Inscription publique : crée le client et ENVOIE l’e-mail de vérification */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (clientRepo.findByEmailIgnoreCase(req.email()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Un compte existe déjà avec cet e-mail.",
                    "errors", Map.of("email", "déjà utilisé")
            ));
        }

        // Mapping RegisterRequest -> ClientRequest (adresse éclatée)
        ClientRequest toCreate = ClientRequest.builder()
                .nom(req.nom())
                .prenom(req.prenom())
                .email(req.email())
                .motDePasse(req.motDePasse())
                .telephone(req.telephone())
                .immatriculation(req.immatriculation())
                .vehiculeMarque(req.vehiculeMarque())
                .vehiculeModele(req.vehiculeModele())
                .vehiculeEnergie(req.vehiculeEnergie())
                .adresseLigne1(req.adresseLigne1())
                .adresseLigne2(req.adresseLigne2())
                .codePostal(req.codePostal())
                .ville(req.ville())
                .build();

        try {
            clientService.create(toCreate, true);
        } catch (DataIntegrityViolationException dup) {
            return ResponseEntity.status(409).body(Map.of(
                    "success", false,
                    "message", "Conflit de données",
                    "errors", Map.of("email", "déjà utilisé")
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Inscription reçue. Vérifiez votre e-mail."
        ));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resend(Authentication auth) {
        String email = auth.getName();
        Client c = clientRepo.findByEmailIgnoreCase(email).orElseThrow();
        if (c.isEmailVerified()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Adresse e-mail déjà vérifiée."));
        }
        emailVerificationService.sendVerificationForClient(c.getIdClient());
        return ResponseEntity.ok(Map.of("message", "E-mail de vérification renvoyé."));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verify(@RequestParam("token") String token) {
        boolean ok = emailVerificationService.verify(token);
        if (!ok) return ResponseEntity.badRequest().body(Map.of("message", "Lien invalide ou expiré"));
        return ResponseEntity.status(302)
                .header("Location", "http://localhost:4200/login?verified=1")
                .build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequest req) {
        try {
            passwordResetService.requestReset(req.email());
        } catch (IllegalArgumentException ignored) {
        }
        return ResponseEntity.ok(Map.of("message", "Si un compte existe pour cet e-mail, un message a été envoyé."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequest req) {
        try {
            passwordResetService.resetPassword(req.token(), req.newPassword(), req.confirmPassword());
            return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé. Vous pouvez vous connecter."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
