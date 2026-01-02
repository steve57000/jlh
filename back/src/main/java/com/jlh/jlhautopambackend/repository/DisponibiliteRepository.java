package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.Disponibilite;
import com.jlh.jlhautopambackend.modeles.DisponibiliteKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisponibiliteRepository
        extends JpaRepository<Disponibilite, DisponibiliteKey> {
}