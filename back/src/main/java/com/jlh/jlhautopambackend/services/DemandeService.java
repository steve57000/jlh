package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ClientStatsDto;
import com.jlh.jlhautopambackend.dto.DemandeRequest;
import com.jlh.jlhautopambackend.dto.DemandeResponse;
import com.jlh.jlhautopambackend.dto.ProchainRdvDto;

import java.util.List;
import java.util.Optional;

public interface DemandeService {

    ClientStatsDto findStatsByClientId(Integer clientId);
    java.util.Optional<ProchainRdvDto> findProchainRdvByClientId(Integer clientId);

    /** Création “classique”, utilisée par l’admin */
    DemandeResponse create(DemandeRequest request);

    /** Création “publique” (pas de payload), appelée depuis DemandeController POST */
    DemandeResponse createPublic();

    /** Création pour un client authentifié */
    DemandeResponse createForClient(Integer clientId, DemandeRequest request);

    Optional<DemandeResponse> findById(Integer id);
    List<DemandeResponse> findAll();

    /** Liste les demandes d’un client */
    List<DemandeResponse> findByClientId(Integer clientId);

    Optional<DemandeResponse> update(Integer id, DemandeRequest request);
    boolean delete(Integer id);

    java.util.Optional<String> buildProchainRendezVousIcs(Integer clientId);
    java.util.Optional<String> buildRendezVousIcs(Integer rdvId, Integer clientIdOrNullIfAdmin);

    Optional<Integer> findCurrentIdForClient(Integer clientId);
    Optional<DemandeResponse> findCurrentForClient(Integer clientId);

    /** Retourne la demande courante si elle existe, sinon la crée */
    DemandeResponse getOrCreateForClient(Integer clientId);

}
