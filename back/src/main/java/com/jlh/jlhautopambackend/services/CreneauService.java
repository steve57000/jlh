package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.CreneauRequest;
import com.jlh.jlhautopambackend.dto.CreneauResponse;

import java.util.List;
import java.util.Optional;

public interface CreneauService {
    CreneauResponse create(CreneauRequest request);
    Optional<CreneauResponse> findById(Integer id);
    List<CreneauResponse> findAll();
    Optional<CreneauResponse> update(Integer id, CreneauRequest request);
    boolean delete(Integer id);
}
