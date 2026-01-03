package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.StatutRendezVous;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatutRendezVousRepository
        extends JpaRepository<StatutRendezVous, String> {
}