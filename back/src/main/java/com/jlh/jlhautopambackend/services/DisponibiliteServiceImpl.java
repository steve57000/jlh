package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.modeles.Disponibilite;
import com.jlh.jlhautopambackend.modeles.DisponibiliteKey;
import com.jlh.jlhautopambackend.repository.AdministrateurRepository;
import com.jlh.jlhautopambackend.repository.CreneauRepository;
import com.jlh.jlhautopambackend.repository.DisponibiliteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DisponibiliteServiceImpl implements DisponibiliteService {

    private final DisponibiliteRepository dispoRepo;
    private final AdministrateurRepository adminRepo;
    private final CreneauRepository creneauRepo;

    public DisponibiliteServiceImpl(DisponibiliteRepository dispoRepo,
                                    AdministrateurRepository adminRepo,
                                    CreneauRepository creneauRepo) {
        this.dispoRepo = dispoRepo;
        this.adminRepo = adminRepo;
        this.creneauRepo = creneauRepo;
    }

    @Override
    public List<Disponibilite> findAll() {
        return dispoRepo.findAll();
    }

    @Override
    public Optional<Disponibilite> findByKey(Integer adminId, Integer creneauId) {
        return dispoRepo.findById(new DisponibiliteKey(adminId, creneauId));
    }

    @Override
    public Disponibilite create(Disponibilite dto) {
        Integer adminId = dto.getAdministrateur().getIdAdmin();
        Integer creneauId = dto.getCreneau().getIdCreneau();

        var admin = adminRepo.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Administrateur introuvable"));
        var creneau = creneauRepo.findById(creneauId)
                .orElseThrow(() -> new IllegalArgumentException("Créneau introuvable"));

        Disponibilite toSave = Disponibilite.builder()
                .id(new DisponibiliteKey(adminId, creneauId))
                .administrateur(admin)
                .creneau(creneau)
                .build();

        return dispoRepo.save(toSave);
    }

    @Override
    public boolean delete(Integer adminId, Integer creneauId) {
        var key = new DisponibiliteKey(adminId, creneauId);
        if (!dispoRepo.existsById(key)) {
            return false;
        }
        dispoRepo.deleteById(key);
        return true;
    }
}
