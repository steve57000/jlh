package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ClientRequest;
import com.jlh.jlhautopambackend.dto.ClientResponse;

import java.util.List;
import java.util.Optional;

public interface ClientService {
    // Méthode historique — par défaut on enverra l’e‑mail (voir Impl)
    ClientResponse create(ClientRequest request);

    // Nouvelle méthode : contrôle explicite de l’envoi de l’e‑mail de vérification
    ClientResponse create(ClientRequest request, boolean sendVerification);

    Optional<ClientResponse> findById(Integer id);
    List<ClientResponse> findAll();
    Optional<ClientResponse> update(Integer id, ClientRequest request);
    boolean delete(Integer id);
}
