// src/main/java/com/jlh/jlhautopambackend/controllers/MeController.java
package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.ChangePasswordRequest;
import com.jlh.jlhautopambackend.dto.ClientMeDto;
import com.jlh.jlhautopambackend.dto.ClientMeUpdateRequest;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final ClientRepository clientRepo;
    private final PasswordEncoder passwordEncoder;

    // --- Changer mon mot de passe (AUTH REQUISE) ---
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(Authentication auth,
                                            @RequestBody @Valid ChangePasswordRequest req) {
        String email = auth.getName();
        Client cli = clientRepo.findByEmail(email).orElseThrow();

        if (!req.newPassword().equals(req.confirmPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "La confirmation ne correspond pas."));
        }
        if (!passwordEncoder.matches(req.oldPassword(), cli.getMotDePasse())) {
            return ResponseEntity.status(403).body(Map.of("message", "Ancien mot de passe incorrect."));
        }

        cli.setMotDePasse(passwordEncoder.encode(req.newPassword()));
        clientRepo.save(cli);
        return ResponseEntity.ok(Map.of("message", "Mot de passe mis à jour."));
    }

    // --- Récupérer mon profil ---
    @GetMapping
    public ResponseEntity<ClientMeDto> me(Authentication auth) {
        String email = auth.getName();
        Client c = clientRepo.findByEmail(email).orElseThrow();

        ClientMeDto dto = ClientMeDto.builder()
                .idClient(c.getIdClient())
                .nom(c.getNom())
                .prenom(c.getPrenom())
                .email(c.getEmail())
                .telephone(c.getTelephone())
                .immatriculation(c.getImmatriculation())
                .adresse(ClientMeDto.AddressDto.builder()
                        .ligne1(c.getAdresseLigne1())
                        .ligne2(c.getAdresseLigne2())
                        .codePostal(c.getAdresseCodePostal())
                        .ville(c.getAdresseVille())
                        .build())
                .build();

        return ResponseEntity.ok(dto);
    }

    // --- MAJ partielle de mon profil (tél, immat, adresse) ---
    @PatchMapping
    public ResponseEntity<ClientMeDto> updateMe(Authentication auth,
                                                @RequestBody @Valid ClientMeUpdateRequest req) {
        String email = auth.getName();
        Client c = clientRepo.findByEmail(email).orElseThrow();

        c.setTelephone(nullIfBlank(req.getTelephone()));
        c.setImmatriculation(
                req.getImmatriculation() == null ? null : req.getImmatriculation().trim().toUpperCase()
        );

        if (req.getAdresse() != null) {
            c.setAdresseLigne1(nullIfBlank(req.getAdresse().getLigne1()));
            c.setAdresseLigne2(nullIfBlank(req.getAdresse().getLigne2()));
            c.setAdresseCodePostal(nullIfBlank(req.getAdresse().getCodePostal()));
            c.setAdresseVille(nullIfBlank(req.getAdresse().getVille()));
        }

        clientRepo.save(c);

        ClientMeDto dto = ClientMeDto.builder()
                .idClient(c.getIdClient())
                .nom(c.getNom())
                .prenom(c.getPrenom())
                .email(c.getEmail())
                .telephone(c.getTelephone())
                .immatriculation(c.getImmatriculation())
                .adresse(ClientMeDto.AddressDto.builder()
                        .ligne1(c.getAdresseLigne1())
                        .ligne2(c.getAdresseLigne2())
                        .codePostal(c.getAdresseCodePostal())
                        .ville(c.getAdresseVille())
                        .build())
                .build();

        return ResponseEntity.ok(dto);
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
