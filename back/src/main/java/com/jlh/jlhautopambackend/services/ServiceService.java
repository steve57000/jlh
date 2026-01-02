package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.*;

import java.util.List;
import java.util.Optional;

public interface ServiceService {
    List<ServiceResponse> findAll();
    Optional<ServiceResponse> findById(Integer id);
    ServiceResponse create(ServiceRequest request);
    Optional<ServiceResponse> update(Integer id, ServiceRequest request);
    boolean delete(Integer id);
}