package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.RendezVous;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Integer> {

    // ✅ chemins d’associations corrects
    boolean existsByDemande_IdDemande(Integer idDemande);
    boolean existsByCreneau_IdCreneau(Integer idCreneau);

    @Query("""
      select rv from RendezVous rv
        join rv.demande d
        join fetch rv.statut rvs
        join fetch rv.creneau cr
      where d.client.idClient = :clientId
        and cr.dateDebut >= :now
      order by cr.dateDebut asc
    """)
    List<RendezVous> findUpcomingByClientId(@Param("clientId") Integer clientId,
                                            @Param("now") Instant now);

    @Query("""
      select count(rv) from RendezVous rv
        join rv.demande d
        join rv.creneau cr
      where d.client.idClient = :clientId
        and cr.dateDebut >= :now
    """)
    long countUpcomingByClientId(@Param("clientId") Integer clientId,
                                 @Param("now") Instant now);

    @Query("""
      select rv from RendezVous rv
        join rv.demande d
        join fetch rv.statut
        join fetch rv.creneau
      where rv.idRdv = :rdvId
        and d.client.idClient = :clientId
    """)
    Optional<RendezVous> findByIdAndClient(@Param("rdvId") Integer rdvId,
                                           @Param("clientId") Integer clientId);
}
