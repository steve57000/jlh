package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    long deleteByClient_IdClient(Integer clientId);
}
