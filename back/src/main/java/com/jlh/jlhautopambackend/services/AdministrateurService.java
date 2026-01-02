package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.AdministrateurRequest;
import com.jlh.jlhautopambackend.dto.AdministrateurResponse;

import java.util.List;
import java.util.Optional;

public interface AdministrateurService {
    AdministrateurResponse create(AdministrateurRequest request);
    Optional<AdministrateurResponse> findById(Integer id);
    List<AdministrateurResponse> findAll();
    Optional<AdministrateurResponse> update(Integer id, AdministrateurRequest request);
    boolean delete(Integer id);
}