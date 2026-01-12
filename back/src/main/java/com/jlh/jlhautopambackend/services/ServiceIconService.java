package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ServiceIconRequest;
import com.jlh.jlhautopambackend.dto.ServiceIconResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ServiceIconService {
    List<ServiceIconResponse> findAll();

    Optional<ServiceIconResponse> create(ServiceIconRequest request);

    Optional<ServiceIconResponse> createFromFile(MultipartFile file, String label);

    Optional<ServiceIconResponse> update(Integer id, ServiceIconRequest request);

    Optional<ServiceIconResponse> updateFromFile(Integer id, MultipartFile file, String label);

    boolean delete(Integer id);

    com.jlh.jlhautopambackend.modeles.ServiceIcon resolveIcon(Integer iconId);
}
