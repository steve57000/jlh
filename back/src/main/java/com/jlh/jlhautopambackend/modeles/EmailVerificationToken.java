package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="email_verification_token", indexes={
        @Index(name="idx_evt_token", columnList="token", unique=true),
        @Index(name="idx_evt_client", columnList="id_client")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailVerificationToken {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="id_client")
    private Client client;

    @Column(nullable=false, unique=true, length=200)
    private String token;            // random base64url

    @Column(nullable=false)
    private Instant expiresAt;

    private Instant consumedAt;
}
