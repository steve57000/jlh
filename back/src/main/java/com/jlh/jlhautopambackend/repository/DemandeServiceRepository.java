// com/jlh/jlhautopambackend/repository/DemandeServiceRepository.java
package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.DemandeService;
import com.jlh.jlhautopambackend.modeles.DemandeServiceKey;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandeServiceRepository extends JpaRepository<DemandeService, DemandeServiceKey> {

    /** Nombre de lignes encore présentes pour une demande donnée. */
    long countByDemande_IdDemande(Integer idDemande);

    /** (optionnel) Supprimer toutes les lignes d’une demande si jamais tu en as besoin ailleurs */
    void deleteById_IdDemande(Integer idDemande);

    boolean existsByService_IdService(Integer idService);

    List<DemandeService> findByService_IdService(Integer idService);

    java.util.Optional<DemandeService> findByDemande_IdDemandeAndService_IdService(Integer demandeId, Integer serviceId);
}
