package com.jlh.jlhautopambackend.repositories;

import com.jlh.jlhautopambackend.modeles.TypeDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeDemandeRepository
        extends JpaRepository<TypeDemande, String> {
}