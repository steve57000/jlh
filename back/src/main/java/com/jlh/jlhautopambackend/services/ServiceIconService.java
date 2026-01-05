package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ServiceIconRequest;
import com.jlh.jlhautopambackend.dto.ServiceIconResponse;

import java.util.List;
import java.util.Optional;

public interface ServiceIconService {
    List<ServiceIconResponse> findAll();

    Optional<ServiceIconResponse> create(ServiceIconRequest request);

    boolean delete(Integer id);

    void ensureIconExists(String url);
}
