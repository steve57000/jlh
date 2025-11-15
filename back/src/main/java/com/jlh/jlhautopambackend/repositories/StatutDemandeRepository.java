package com.jlh.jlhautopambackend.repositories;

import com.jlh.jlhautopambackend.modeles.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatutDemandeRepository
        extends JpaRepository<StatutDemande, String> {
}
