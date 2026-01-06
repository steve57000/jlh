package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.Devis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DevisRepository extends JpaRepository<Devis, Integer> {
    java.util.Optional<Devis> findByDemande_IdDemande(Integer idDemande);
}
