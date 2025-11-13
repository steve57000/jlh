package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.modeles.Promotion;
import com.jlh.jlhautopambackend.repository.PromotionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;

@Service
public class PromotionCleanupService {

    private static final Logger log = LoggerFactory.getLogger(PromotionCleanupService.class);

    private final PromotionRepository promoRepo;
    private final Path uploadDir;

    public PromotionCleanupService(PromotionRepository promoRepo,
                                   @Value("${app.upload-dir}") String uploadDir) {
        this.promoRepo = promoRepo;
        this.uploadDir = Paths.get(uploadDir);
    }

    /**
     * Supprime chaque jour à 23h59 toutes les promotions expirées
     * et leurs images stockées sur le disque.
     */
    @Scheduled(cron = "0 59 23 * * *")
    @Transactional
    public void removeExpiredPromotions() {
        Instant now = Instant.now();
        // 1. Récupère d'abord les promotions expirées
        List<Promotion> expired = promoRepo.findByValidToBefore(now);

        for (Promotion promo : expired) {
            String imageUrl = promo.getImageUrl();
            String filename = extractFilename(imageUrl);
            Path filePath = uploadDir.resolve(filename);
            try {
                if (Files.deleteIfExists(filePath)) {
                    log.info("Image supprimée : {}", filePath);
                } else {
                    log.warn("Image non trouvée pour suppression : {}", filePath);
                }
            } catch (IOException e) {
                log.error("Erreur lors de la suppression de l’image {} : {}", filePath, e.getMessage(), e);
            }
        }

        // 2. Puis supprime définitivement les promotions en base
        promoRepo.deleteAll(expired);
        log.info("{} promotions expirées supprimées.", expired.size());
    }

    /**
     * Extrait le nom de fichier à partir de l’URL (tout ce qui suit le dernier '/').
     */
    private String extractFilename(String imageUrl) {
        try {
            URI uri = URI.create(imageUrl);
            String path = uri.getPath();
            return Paths.get(path).getFileName().toString();
        } catch (Exception e) {
            log.error("Impossible d’extraire le nom de fichier de l’URL : {}", imageUrl, e);
            // si échec, on renvoie l’intégralité de l’URL en nom (improbable mais sûr)
            return imageUrl;
        }
    }
}
