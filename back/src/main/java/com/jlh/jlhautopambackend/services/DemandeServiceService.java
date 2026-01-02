package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.*;
import java.util.*;

public interface DemandeServiceService {
    List<DemandeServiceResponse> findAll();
    Optional<DemandeServiceResponse> findByKey(Integer demandeId, Integer serviceId);
    DemandeServiceResponse create(DemandeServiceRequest req);
    Optional<DemandeServiceResponse> update(Integer demandeId, Integer serviceId, DemandeServiceRequest req);
    boolean delete(Integer demandeId, Integer serviceId);
}
