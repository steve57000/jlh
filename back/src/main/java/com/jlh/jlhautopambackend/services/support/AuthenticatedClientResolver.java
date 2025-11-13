package com.jlh.jlhautopambackend.services.support;

import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Petit service utilitaire permettant de factoriser la résolution du client
 * courant dans les contrôleurs.
 */
@Component
public class AuthenticatedClientResolver {

    private final ClientRepository clientRepository;

    public AuthenticatedClientResolver(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * @return le client correspondant à l'authentification courante
     * @throws IllegalArgumentException si aucun client ne correspond à l'e-mail porté par le token
     */
    public Client requireCurrentClient(Authentication authentication) {
        String email = authentication.getName();
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable"));
    }
}

