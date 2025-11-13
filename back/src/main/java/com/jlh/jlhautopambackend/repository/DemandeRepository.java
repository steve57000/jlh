package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.Demande;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Integer> {

    long countByClient_IdClientAndStatutDemande_CodeStatut(Integer clientId, String codeStatut);
    List<Demande> findByClient_IdClientOrderByDateDemandeDesc(Integer clientId);

    @EntityGraph(attributePaths = {
            "client",
            "typeDemande",
            "statutDemande",
            "services",
            "services.service",   // libellé + prix
            "rendezVous",
            "rendezVous.statut"
    })
    List<Demande> findAll();

    @EntityGraph(attributePaths = {
            "client",
            "typeDemande",
            "statutDemande",
            "services",
            "services.service",
            "rendezVous",
            "rendezVous.statut"
    })
    Optional<Demande> findById(Integer id);

    @EntityGraph(attributePaths = {
            "client",
            "typeDemande",
            "statutDemande",
            "services",
            "services.service",
            "rendezVous",
            "rendezVous.statut"
    })
    List<Demande> findByClient_IdClient(Integer clientId);


    // Variante 2 — Demande a une relation @ManyToOne Client client
     Optional<Demande> findFirstByClient_IdClientAndStatutDemande_CodeStatutOrderByDateDemandeDesc(
            Integer clientId, String codeStatut);
}
