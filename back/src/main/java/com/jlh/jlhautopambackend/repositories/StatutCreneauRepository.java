package com.jlh.jlhautopambackend.repositories;

import com.jlh.jlhautopambackend.modeles.StatutCreneau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatutCreneauRepository
        extends JpaRepository<StatutCreneau, String> {
}