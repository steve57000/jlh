package com.jlh.jlhautopambackend.repositories;

import com.jlh.jlhautopambackend.modeles.Devis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DevisRepository extends JpaRepository<Devis, Integer> {
}