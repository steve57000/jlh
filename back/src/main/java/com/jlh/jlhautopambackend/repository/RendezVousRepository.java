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
        join rv.demande d
        join d.client c
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
        join rv.demande d
        join d.client c
        join rv.creneau cr
      where c.idClient = :clientId
        and cr.dateDebut >= :now
    """)
    long countUpcomingByClientId(@Param("clientId") Integer clientId,
                                 @Param("now") Instant now);

    @Query("""
      select rv from RendezVous rv
        join rv.demande d
        join d.client c
        join fetch rv.statut
        join fetch rv.creneau
      where rv.idRdv = :rdvId
        and c.idClient = :clientId
    """)
    Optional<RendezVous> findByIdAndClient(@Param("rdvId") Integer rdvId,
                                           @Param("clientId") Integer clientId);

    @Query("""
      select count(rv) from RendezVous rv
        join rv.demande d
        join d.client c
      where c.idClient = :clientId
        and rv.demandeService is null
        and rv.devis is null
    """)
    long countByClient_IdClientAndDemandeServiceIsNullAndDevisIsNull(@Param("clientId") Integer clientId);

    long countByDemande_Client_IdClientAndDemandeServiceIsNullAndDevisIsNull(Integer clientId);

    @Query("""
      select count(rv) from RendezVous rv
      where rv.demande.client.idClient = :clientId
        and (rv.demandeService is not null or rv.devis is not null)
    """)
    long countLinkedByClientId(@Param("clientId") Integer clientId);

    @Query("""
      select function('date_part', 'year', cr.dateDebut) as year,
             count(rv) as count
      from RendezVous rv
        join rv.creneau cr
      group by function('date_part', 'year', cr.dateDebut)
    """)
    List<YearlyCount> aggregateYearlyRendezVousStats();

    interface YearlyCount {
        Integer getYear();
        Long getCount();
    }
}
