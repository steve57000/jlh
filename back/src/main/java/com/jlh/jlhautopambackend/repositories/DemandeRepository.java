package com.jlh.jlhautopambackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jlh.jlhautopambackend.modeles.Demande;

public interface DemandeRepository extends JpaRepository<Demande, Integer> { }