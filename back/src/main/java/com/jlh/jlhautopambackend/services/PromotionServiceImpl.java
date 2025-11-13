package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.PromotionRequest;
import com.jlh.jlhautopambackend.dto.PromotionResponse;
import com.jlh.jlhautopambackend.mapper.PromotionMapper;
import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.modeles.Promotion;
import com.jlh.jlhautopambackend.repository.AdministrateurRepository;
import com.jlh.jlhautopambackend.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promoRepo;
    private final AdministrateurRepository adminRepo;
    private final PromotionMapper mapper;

    /** Chemin absolu sur le disque où stocker les images */
    @Value("${app.upload-dir}")
    private String uploadDir;

    public PromotionServiceImpl(PromotionRepository promoRepo,
                                AdministrateurRepository adminRepo,
                                PromotionMapper mapper) {
        this.promoRepo = promoRepo;
        this.adminRepo = adminRepo;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> findAll() {
        return promoRepo.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PromotionResponse> findById(Integer id) {
        return promoRepo.findById(id)
                .map(mapper::toResponse);
    }

    /** Création sans fichier, utilisée pour les tests */
    public PromotionResponse create(PromotionRequest req) {
        validateDates(req.getValidFrom(), req.getValidTo());
        Administrateur admin = loadAdmin(req.getAdministrateurId());
        Promotion entity = mapper.toEntity(req);
        entity.setAdministrateur(admin);
        Promotion saved = promoRepo.save(entity);
        return mapper.toResponse(saved);
    }

    /** Mise à jour sans fichier, utilisée pour les tests */
    public Optional<PromotionResponse> update(Integer id, PromotionRequest req) {
        Optional<Promotion> opt = promoRepo.findById(id);
        if (opt.isEmpty()) return Optional.empty();

        validateDates(req.getValidFrom(), req.getValidTo());
        Promotion existing = opt.get();
        handleAdminChange(existing, req.getAdministrateurId());

        // NE METTRE à jour que si req.imageUrl non-null ET non-blank
        if (StringUtils.hasText(req.getImageUrl())) {
            existing.setImageUrl(req.getImageUrl());
        }

        existing.setValidFrom(req.getValidFrom());
        existing.setValidTo(req.getValidTo());
        existing.setDescription(req.getDescription());

        Promotion saved = promoRepo.save(existing);
        return Optional.of(mapper.toResponse(saved));
    }

    @Override
    public boolean delete(Integer id) {
        if (!promoRepo.existsById(id)) {
            return false;
        }
        // On pourrait supprimer le fichier ici si on stocke le chemin dans la DB
        promoRepo.deleteById(id);
        return true;
    }

    // --- implémentation avec MultipartFile ---

    @Override
    public PromotionResponse create(PromotionRequest req, MultipartFile file) throws IOException {
        // si un fichier est fourni, on le stocke
        if (file != null && !file.isEmpty()) {
            String filename = storeFile(file);
            req.setImageUrl("/promotions/images/" + filename);
        }
        return create(req);
    }

    @Override
    public Optional<PromotionResponse> update(Integer id, PromotionRequest req, MultipartFile file) throws IOException {
        // on supprime l'ancienne image si on remplace
        if (file != null && !file.isEmpty()) {
            // charger l'ancienne entité pour récupérer l'URL
            promoRepo.findById(id).ifPresent(old -> {
                String oldUrl = old.getImageUrl();
                if (oldUrl != null && oldUrl.startsWith("/promotions/images/")) {
                    Path oldPath = Paths.get(uploadDir, oldUrl.substring("/promotions/images/".length()));
                    try { Files.deleteIfExists(oldPath); } catch (IOException ignored) {}
                }
            });
            String filename = storeFile(file);
            req.setImageUrl("/promotions/images/" + filename);
        }
        return update(id, req);
    }

    // --- méthodes utilitaires privées ---

    private void validateDates(Instant from, Instant to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("validFrom doit être avant validTo");
        }
    }

    private Administrateur loadAdmin(Integer adminId) {
        return adminRepo.findById(adminId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Administrateur introuvable : " + adminId)
                );
    }

    private void handleAdminChange(Promotion existing, Integer newAdminId) {
        if (!existing.getAdministrateur().getIdAdmin().equals(newAdminId)) {
            Administrateur newAdmin = loadAdmin(newAdminId);
            existing.setAdministrateur(newAdmin);
        }
    }

    private String storeFile(MultipartFile file) throws IOException {
        // Récupère le nom original, ou "" si null
        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("");

        // Nettoie le chemin pour éviter les séquences malveillantes
        String safeName = StringUtils.cleanPath(originalName);

        // Construit un nom unique : UUID + (–nomNettoyé si présent)
        String filename = UUID.randomUUID()
                + (safeName.isEmpty() ? "" : "-" + safeName);

        // Crée le dossier si besoin
        Path targetDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(targetDir);

        // Transfère le fichier
        Path target = targetDir.resolve(filename);
        file.transferTo(target);
        return filename;
    }
}
