package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    long deleteByClient_IdClient(Integer clientId); // utile pour nettoyer avant renvoi
}
