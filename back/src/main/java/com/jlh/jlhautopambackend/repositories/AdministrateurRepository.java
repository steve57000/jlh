package com.jlh.jlhautopambackend.repositories;

import com.jlh.jlhautopambackend.modeles.Administrateur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministrateurRepository
        extends JpaRepository<Administrateur, Integer> {
}