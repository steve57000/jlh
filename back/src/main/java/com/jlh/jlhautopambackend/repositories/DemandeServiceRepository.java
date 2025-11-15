package com.jlh.jlhautopambackend.repositories;

import com.jlh.jlhautopambackend.modeles.DemandeService;
import com.jlh.jlhautopambackend.modeles.DemandeServiceKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandeServiceRepository
        extends JpaRepository<DemandeService, DemandeServiceKey> {
}
