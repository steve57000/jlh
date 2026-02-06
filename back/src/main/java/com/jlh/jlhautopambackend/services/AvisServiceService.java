package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.AvisServiceModerationRequest;
import com.jlh.jlhautopambackend.dto.AvisServiceRequest;
import com.jlh.jlhautopambackend.dto.AvisServiceResponse;
import com.jlh.jlhautopambackend.dto.AvisServiceUpdateRequest;
import com.jlh.jlhautopambackend.dto.ServiceAvisStatsResponse;
import com.jlh.jlhautopambackend.modeles.AvisServiceStatut;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AvisServiceService {
    AvisServiceResponse create(Integer clientId, AvisServiceRequest request);

    Page<AvisServiceResponse> findApproved(Pageable pageable);

    Page<AvisServiceResponse> findAll(Pageable pageable);

    Page<AvisServiceResponse> findByStatut(AvisServiceStatut statut, Pageable pageable);

    List<AvisServiceResponse> findByService(Integer serviceId);

    List<AvisServiceResponse> findByDemande(Integer demandeId);

    List<AvisServiceResponse> findByClient(Integer clientId);

    Page<AvisServiceResponse> findByService(Integer serviceId, Pageable pageable);

    Page<AvisServiceResponse> findByService(Integer serviceId, AvisServiceStatut statut, Pageable pageable);

    Page<AvisServiceResponse> findApprovedByService(Integer serviceId, Pageable pageable);

    Page<AvisServiceResponse> findByDemande(Integer demandeId, Pageable pageable);

    Page<AvisServiceResponse> findByClient(Integer clientId, Pageable pageable);

    AvisServiceResponse findById(Long idAvis);

    ServiceAvisStatsResponse getServiceStats(Integer serviceId);

    AvisServiceResponse updateByClient(Integer clientId, Long idAvis, AvisServiceUpdateRequest request);

    AvisServiceResponse moderate(Long idAvis, AvisServiceModerationRequest request);
}
