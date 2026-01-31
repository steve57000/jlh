package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.AvisService;
import com.jlh.jlhautopambackend.modeles.AvisServiceStatut;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AvisServiceRepository extends JpaRepository<AvisService, Long> {
    boolean existsByDemande_IdDemandeAndService_IdService(Integer demandeId, Integer serviceId);

    List<AvisService> findByService_IdService(Integer serviceId);

    Page<AvisService> findByService_IdService(Integer serviceId, Pageable pageable);

    List<AvisService> findByService_IdServiceAndStatut(Integer serviceId, AvisServiceStatut statut);

    Page<AvisService> findByService_IdServiceAndStatut(Integer serviceId, AvisServiceStatut statut, Pageable pageable);

    List<AvisService> findByDemande_IdDemande(Integer demandeId);

    Page<AvisService> findByDemande_IdDemande(Integer demandeId, Pageable pageable);

    List<AvisService> findByClient_IdClient(Integer clientId);

    Page<AvisService> findByClient_IdClient(Integer clientId, Pageable pageable);

    @Query("""
        select avg(a.note) as averageNote, count(a) as totalAvis
        from AvisService a
        where a.service.idService = :serviceId
          and a.statut = :statut
    """)
    Optional<ServiceAvisStats> getServiceStats(
            @Param("serviceId") Integer serviceId,
            @Param("statut") AvisServiceStatut statut
    );

    interface ServiceAvisStats {
        Double getAverageNote();
        Long getTotalAvis();
    }
}
