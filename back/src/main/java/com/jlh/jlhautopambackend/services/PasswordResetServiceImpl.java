package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.modeles.PasswordResetToken;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import com.jlh.jlhautopambackend.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
@Transactional
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final ClientRepository clientRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Value("${garage.timezone:Europe/Paris}") String tz;
    @Value("${app.frontendUrl:http://localhost:4200}") String frontendUrl;

    // validité du lien (heures)
    @Value("${app.passwordReset.expiryHours:2}") int resetExpiryHours;

    private String randomToken() {
        byte[] buf = new byte[32];
        new SecureRandom().nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    @Override
    public void requestReset(String email) {
        Client cli = clientRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Aucun compte pour cet e-mail."));

        // nettoyer les anciens tokens de ce client
        tokenRepo.deleteByClient_IdClient(cli.getIdClient());

        var t = PasswordResetToken.builder()
                .client(cli)
                .token(randomToken())
                .expiresAt(Instant.now().plus(resetExpiryHours, ChronoUnit.HOURS))
                .build();
        tokenRepo.save(t);

        String link = frontendUrl + "/reset-password?token=" + t.getToken();

        String html = """
            <p>Bonjour %s,</p>
            <p>Vous avez demandé la réinitialisation de votre mot de passe.</p>
            <p><a href="%s">Cliquez ici pour définir un nouveau mot de passe</a> (valide %d heure(s)).</p>
            <p>Si vous n'êtes pas à l'origine de cette demande, ignorez cet e-mail.</p>
            <p>— JLH Auto Pam</p>
        """.formatted(cli.getPrenom() != null ? cli.getPrenom() : "", link, resetExpiryHours);

        mailService.sendHtml(cli.getEmail(), "Réinitialisation du mot de passe", html);
    }

    @Override
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("La confirmation ne correspond pas.");
        }

        PasswordResetToken t = tokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Lien invalide."));
        if (t.getUsedAt() != null) throw new IllegalArgumentException("Lien déjà utilisé.");
        if (Instant.now().isAfter(t.getExpiresAt())) throw new IllegalArgumentException("Lien expiré.");

        Client cli = t.getClient();
        cli.setMotDePasse(passwordEncoder.encode(newPassword));
        // invalider le token
        t.setUsedAt(Instant.now());
        // la transaction persiste les changements
    }
}
