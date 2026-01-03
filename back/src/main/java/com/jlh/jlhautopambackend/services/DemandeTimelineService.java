package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.DemandeTimelineEntryDto;
import com.jlh.jlhautopambackend.dto.DemandeTimelineRequest;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.RendezVous;
import com.jlh.jlhautopambackend.modeles.StatutDemande;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DemandeTimelineService {

    Optional<List<DemandeTimelineEntryDto>> listForDemande(Integer demandeId, boolean includeInternal);

    DemandeTimelineEntryDto logAdminEvent(Integer demandeId, DemandeTimelineRequest request, String actorUsername);

    void logStatusChange(Demande demande, StatutDemande newStatut, String previousCode, String actorEmail, String actorUsername);

    void logMontantValidation(Demande demande, BigDecimal montant, String commentaire, String actorEmail, String actorUsername);

    void logRendezVousEvent(Demande demande, RendezVous rendezVous, String commentaire, String actorEmail, String actorUsername);
}
