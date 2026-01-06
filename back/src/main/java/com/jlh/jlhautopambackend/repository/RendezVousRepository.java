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
    boolean existsByCreneau_IdCreneau(Integer idCreneau);

    @Query("""
      select rv from RendezVous rv
        join rv.client c
        join fetch rv.statut rvs
        join fetch rv.creneau cr
      where c.idClient = :clientId
        and cr.dateDebut >= :now
      order by cr.dateDebut asc
    """)
    List<RendezVous> findUpcomingByClientId(@Param("clientId") Integer clientId,
                                            @Param("now") Instant now);

    @Query("""
      select count(rv) from RendezVous rv
        join rv.client c
        join rv.creneau cr
      where c.idClient = :clientId
        and cr.dateDebut >= :now
    """)
    long countUpcomingByClientId(@Param("clientId") Integer clientId,
                                 @Param("now") Instant now);

    @Query("""
      select rv from RendezVous rv
        join rv.client c
        join fetch rv.statut
        join fetch rv.creneau
      where rv.idRdv = :rdvId
        and c.idClient = :clientId
    """)
    Optional<RendezVous> findByIdAndClient(@Param("rdvId") Integer rdvId,
                                           @Param("clientId") Integer clientId);

    long countByClient_IdClientAndDemandeServiceIsNullAndDevisIsNull(Integer clientId);

    @Query("""
      select count(rv) from RendezVous rv
      where rv.client.idClient = :clientId
        and (rv.demandeService is not null or rv.devis is not null)
    """)
    long countLinkedByClientId(@Param("clientId") Integer clientId);
}
