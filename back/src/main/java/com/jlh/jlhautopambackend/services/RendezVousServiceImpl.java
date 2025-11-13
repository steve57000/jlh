package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.RendezVousRequest;
import com.jlh.jlhautopambackend.dto.RendezVousResponse;
import com.jlh.jlhautopambackend.mapper.RendezVousMapper;
import com.jlh.jlhautopambackend.modeles.*;
import com.jlh.jlhautopambackend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RendezVousServiceImpl implements RendezVousService {

    private final RendezVousRepository repo;
    private final DemandeRepository demandeRepo;
    private final CreneauRepository creneauRepo;
    private final AdministrateurRepository adminRepo;
    private final StatutRendezVousRepository statutRendezVousRepo;
    private final StatutDemandeRepository statutDemandeRepo;
    private final TypeDemandeRepository typeDemandeRepo;
    private final RendezVousMapper mapper;

    private static final String STATUT_BROUILLON  = "Brouillon";
    private static final String STATUT_EN_ATTENTE = "En_attente";
    private static final String TYPE_RDV          = "RendezVous";

    public RendezVousServiceImpl(RendezVousRepository repo,
                                 DemandeRepository demandeRepo,
                                 CreneauRepository creneauRepo,
                                 AdministrateurRepository adminRepo,
                                 StatutRendezVousRepository statutRendezVousRepo,
                                 StatutDemandeRepository statutDemandeRepo,
                                 TypeDemandeRepository typeDemandeRepo,
                                 RendezVousMapper mapper) {
        this.repo = repo;
        this.demandeRepo = demandeRepo;
        this.creneauRepo = creneauRepo;
        this.adminRepo = adminRepo;
        this.statutRendezVousRepo = statutRendezVousRepo;
        this.statutDemandeRepo = statutDemandeRepo;
        this.typeDemandeRepo = typeDemandeRepo;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponse> findAll() {
        return repo.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RendezVousResponse> findById(Integer id) {
        return repo.findById(id).map(mapper::toResponse);
    }

    @Override
    public RendezVousResponse create(RendezVousRequest req) {
        Demande demande = demandeRepo.findById(req.getDemandeId())
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + req.getDemandeId()));
        Creneau creneau = creneauRepo.findById(req.getCreneauId())
                .orElseThrow(() -> new IllegalArgumentException("Creneau introuvable: " + req.getCreneauId()));
        Administrateur admin = adminRepo.findById(req.getAdministrateurId())
                .orElseThrow(() -> new IllegalArgumentException("Administrateur introuvable: " + req.getAdministrateurId()));
        StatutRendezVous statutRdv = statutRendezVousRepo.findById(req.getCodeStatut())
                .orElseThrow(() -> new IllegalArgumentException("Statut RDV introuvable: " + req.getCodeStatut()));

        // 1) Basculer la demande en type RendezVous si besoin
        if (demande.getTypeDemande() == null || !TYPE_RDV.equals(demande.getTypeDemande().getCodeType())) {
            var typeRdv = typeDemandeRepo.findById(TYPE_RDV)
                    .orElseThrow(() -> new IllegalStateException("TypeDemande 'RendezVous' manquant"));
            demande.setTypeDemande(typeRdv);
            demandeRepo.save(demande);
        }

        // 2) Créer le RDV
        RendezVous ent = mapper.toEntity(req);
        ent.setDemande(demande);
        ent.setCreneau(creneau);
        ent.setAdministrateur(admin);
        ent.setStatut(statutRdv);

        RendezVous saved = repo.save(ent);

        // 3) Brouillon -> En_attente si nécessaire
        String current = demande.getStatutDemande() != null ? demande.getStatutDemande().getCodeStatut() : null;
        if (current == null || STATUT_BROUILLON.equals(current)) {
            var enAttente = statutDemandeRepo.findById(STATUT_EN_ATTENTE)
                    .orElseThrow(() -> new IllegalStateException("Statut 'En_attente' manquant en base"));
            demande.setStatutDemande(enAttente);
            demandeRepo.save(demande);
        }

        return mapper.toResponse(saved);
    }

    @Override
    public Optional<RendezVousResponse> update(Integer id, RendezVousRequest req) {
        return repo.findById(id).map(existing -> {
            existing.setDemande(demandeRepo.findById(req.getDemandeId())
                    .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + req.getDemandeId())));
            existing.setCreneau(creneauRepo.findById(req.getCreneauId())
                    .orElseThrow(() -> new IllegalArgumentException("Creneau introuvable: " + req.getCreneauId())));
            existing.setAdministrateur(adminRepo.findById(req.getAdministrateurId())
                    .orElseThrow(() -> new IllegalArgumentException("Administrateur introuvable: " + req.getAdministrateurId())));
            existing.setStatut(statutRendezVousRepo.findById(req.getCodeStatut())
                    .orElseThrow(() -> new IllegalArgumentException("Statut RDV introuvable: " + req.getCodeStatut())));
            RendezVous updated = repo.save(existing);
            return mapper.toResponse(updated);
        });
    }

    @Override
    public Optional<RendezVousResponse> submit(Integer rdvId, Integer clientIdOrNullIfAdmin) {
        Optional<RendezVous> opt = (clientIdOrNullIfAdmin == null)
                ? repo.findById(rdvId) // ADMIN
                : repo.findByIdAndClient(rdvId, clientIdOrNullIfAdmin); // CLIENT (ownership)

        if (opt.isEmpty()) return Optional.empty();

        RendezVous rdv = opt.get();
        Demande demande = rdv.getDemande();
        if (demande == null) return Optional.empty();

        String current = demande.getStatutDemande() != null ? demande.getStatutDemande().getCodeStatut() : null;
        if (current == null || STATUT_BROUILLON.equals(current)) {
            StatutDemande enAttente = statutDemandeRepo.findById(STATUT_EN_ATTENTE)
                    .orElseThrow(() -> new IllegalStateException("Statut 'En_attente' manquant en base"));
            demande.setStatutDemande(enAttente);
            demandeRepo.save(demande);
        }

        return Optional.of(mapper.toResponse(rdv));
    }

    @Override
    public boolean delete(Integer id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }
}
