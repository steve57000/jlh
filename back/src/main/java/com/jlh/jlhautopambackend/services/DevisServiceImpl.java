package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.DevisRequest;
import com.jlh.jlhautopambackend.dto.DevisResponse;
import com.jlh.jlhautopambackend.mapper.DevisMapper;
import com.jlh.jlhautopambackend.modeles.Devis;
import com.jlh.jlhautopambackend.repository.DevisRepository;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DevisServiceImpl implements DevisService {

    private final DevisRepository devisRepo;
    private final DemandeRepository demandeRepo;
    private final DevisMapper mapper;

    public DevisServiceImpl(DevisRepository devisRepo,
                            DemandeRepository demandeRepo,
                            DevisMapper mapper) {
        this.devisRepo = devisRepo;
        this.demandeRepo = demandeRepo;
        this.mapper = mapper;
    }

    @Override
    public List<DevisResponse> findAll() {
        return devisRepo.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DevisResponse> findById(Integer id) {
        return devisRepo.findById(id)
                .map(mapper::toResponse);
    }

    @Override
    public DevisResponse create(DevisRequest request) {
        var demande = demandeRepo.findById(request.getDemandeId())
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));

        Devis entity = mapper.toEntity(request);
        entity.setDemande(demande);

        if (entity.getDateDevis() == null) {
            entity.setDateDevis(Instant.now());
        }

        // Si montantTotal n'est pas fourni mais main d'œuvre et/ou pièces oui, on recalcule
        if (entity.getMontantTotal() == null &&
                (entity.getMontantMainOeuvre() != null || entity.getMontantPieces() != null)) {
            BigDecimal main = entity.getMontantMainOeuvre() != null ? entity.getMontantMainOeuvre() : BigDecimal.ZERO;
            BigDecimal pieces = entity.getMontantPieces() != null ? entity.getMontantPieces() : BigDecimal.ZERO;
            entity.setMontantTotal(main.add(pieces));
        }

        Devis saved = devisRepo.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public Optional<DevisResponse> update(Integer id, DevisRequest request) {
        return devisRepo.findById(id)
                .map(existing -> {
                    // Main d'œuvre
                    if (request.getMontantMainOeuvre() != null) {
                        existing.setMontantMainOeuvre(request.getMontantMainOeuvre());
                    }

                    // Pièces
                    if (request.getMontantPieces() != null) {
                        existing.setMontantPieces(request.getMontantPieces());
                    }

                    // Montant total : si fourni, on prend la valeur du front,
                    // sinon on recalcule à partir de main d'œuvre + pièces.
                    if (request.getMontantTotal() != null) {
                        existing.setMontantTotal(request.getMontantTotal());
                    } else {
                        BigDecimal main = existing.getMontantMainOeuvre() != null ? existing.getMontantMainOeuvre() : BigDecimal.ZERO;
                        BigDecimal pieces = existing.getMontantPieces() != null ? existing.getMontantPieces() : BigDecimal.ZERO;
                        existing.setMontantTotal(main.add(pieces));
                    }

                    Devis updated = devisRepo.save(existing);
                    return mapper.toResponse(updated);
                });
    }

    @Override
    public boolean delete(Integer id) {
        if (!devisRepo.existsById(id)) {
            return false;
        }
        devisRepo.deleteById(id);
        return true;
    }
}
