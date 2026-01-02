package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.modeles.Disponibilite;

import java.util.List;
import java.util.Optional;

public interface DisponibiliteService {
    List<Disponibilite> findAll();
    Optional<Disponibilite> findByKey(Integer adminId, Integer creneauId);
    Disponibilite create(Disponibilite dto);
    boolean delete(Integer adminId, Integer creneauId);
}