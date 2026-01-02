package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.DevisRequest;
import com.jlh.jlhautopambackend.dto.DevisResponse;

import java.util.List;
import java.util.Optional;

public interface DevisService {

    List<DevisResponse> findAll();

    Optional<DevisResponse> findById(Integer id);

    DevisResponse create(DevisRequest request);

    Optional<DevisResponse> update(Integer id, DevisRequest request);

    boolean delete(Integer id);
}
