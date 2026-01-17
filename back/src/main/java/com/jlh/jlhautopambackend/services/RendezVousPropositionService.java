package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.RendezVousPropositionBatchRequest;
import com.jlh.jlhautopambackend.dto.RendezVousPropositionResponse;
import java.util.List;

public interface RendezVousPropositionService {
    List<RendezVousPropositionResponse> listByDemande(Integer demandeId, Integer clientIdOrNull);

    List<RendezVousPropositionResponse> createForDemande(
            Integer demandeId,
            RendezVousPropositionBatchRequest request,
            Integer adminId
    );

    RendezVousPropositionResponse accept(Integer demandeId, Integer propositionId, Integer clientIdOrNull, Integer adminIdOrNull);

    RendezVousPropositionResponse decline(Integer demandeId, Integer propositionId, Integer clientIdOrNull, Integer adminIdOrNull);
}
