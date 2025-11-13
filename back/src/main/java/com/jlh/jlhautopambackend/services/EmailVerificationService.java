package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.modeles.*;
import com.jlh.jlhautopambackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service @RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepo;
    private final ClientRepository clientRepo;
    private final MailService mailService;

    @Value("${app.baseUrl}") String baseUrl;
    @Value("${app.email.verificationExpiryHours:24}") int expiryHours;

    private static String randomToken() {
        byte[] buf = new byte[32];
        new SecureRandom().nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    @Transactional
    public void sendVerificationForClient(Integer clientId) {
        var client = clientRepo.findById(clientId).orElseThrow(() -> new IllegalArgumentException("Client introuvable"));
        // nettoyer les anciens tokens
        tokenRepo.deleteByClient_IdClient(clientId);

        var token = EmailVerificationToken.builder()
                .client(client)
                .token(randomToken())
                .expiresAt(Instant.now().plusSeconds(expiryHours * 3600L))
                .build();
        tokenRepo.save(token);

        String link = baseUrl + "/api/auth/verify-email?token=" + token.getToken();
        String html = """
      <p>Bonjour %s,</p>
      <p>Merci de confirmer votre adresse e-mail pour activer votre compte JLH Auto Pam.</p>
      <p><a href="%s">Confirmer mon e-mail</a></p>
      <p>Ce lien expire dans %d heure(s).</p>
      <p>À bientôt,<br/>JLH Auto Pam</p>
    """.formatted(client.getPrenom()!=null?client.getPrenom():client.getNom(), link, expiryHours);

        mailService.sendHtml(client.getEmail(), "Confirmez votre e-mail", html);
    }

    @Transactional
    public boolean verify(String tokenValue) {
        var opt = tokenRepo.findByToken(tokenValue);
        if (opt.isEmpty()) return false;

        var tok = opt.get();
        if (tok.getConsumedAt()!=null || tok.getExpiresAt().isBefore(Instant.now())) return false;

        var client = tok.getClient();
        client.setEmailVerified(true);
        client.setEmailVerifiedAt(Instant.now());
        clientRepo.save(client);

        tok.setConsumedAt(Instant.now());
        tokenRepo.save(tok);
        return true;
    }
}
