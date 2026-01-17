package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.RendezVousProposition;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RendezVousPropositionRepository extends JpaRepository<RendezVousProposition, Integer> {
    List<RendezVousProposition> findByDemande_IdDemandeOrderByDateDebutAsc(Integer demandeId);

    Optional<RendezVousProposition> findByIdPropositionAndDemande_IdDemande(Integer idProposition, Integer demandeId);

    List<RendezVousProposition> findByDemande_IdDemandeAndStatut(Integer demandeId, String statut);

    long countByDemande_IdDemandeAndStatutAndExpiresAtAfter(Integer demandeId, String statut, Instant now);
}
