package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.modeles.Promotion;
import com.jlh.jlhautopambackend.repository.PromotionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class PromotionCleanupService {

    private static final Logger log = LoggerFactory.getLogger(PromotionCleanupService.class);

    private final PromotionRepository promoRepo;
    private final Path uploadDir;
    private final ZoneId zoneId;

    public PromotionCleanupService(PromotionRepository promoRepo,
                                   @Value("${app.upload-dir}") String uploadDir,
                                   @Value("${garage.timezone:Europe/Paris}") String timezone) {
        this.promoRepo = promoRepo;
        this.uploadDir = Paths.get(uploadDir);
        this.zoneId = ZoneId.of(timezone);
    }

    /**
     * Supprime chaque jour à 23h59 toutes les promotions expirées
     * et leurs images stockées sur le disque.
     */
    @Scheduled(cron = "0 59 23 * * *", zone = "${garage.timezone:Europe/Paris}")
    @Transactional
    public void removeExpiredPromotions() {
        // Supprime les promotions expirées à 23h59 heure locale (Europe/Paris par défaut).
        Instant cutoff = endOfToday();
        // 1. Récupère d'abord les promotions expirées
        List<Promotion> expired = promoRepo.findByValidToLessThanEqual(cutoff);

        for (Promotion promo : expired) {
            String imageUrl = promo.getImageUrl();
            if (!StringUtils.hasText(imageUrl)) {
                continue;
            }

            String filename = extractFilename(imageUrl);
            if (!StringUtils.hasText(filename)) {
                continue;
            }

            Path filePath = uploadDir.resolve("images").resolve(filename);
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
        log.info("{} promotions expirées supprimées (validTo <= {}).", expired.size(), cutoff);
    }

    /**
     * Extrait le nom de fichier à partir de l’URL (tout ce qui suit le dernier '/').
     */
    private String extractFilename(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return "";
        }

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

    private Instant endOfToday() {
        LocalDate today = ZonedDateTime.now(zoneId).toLocalDate();
        return today.plusDays(1).atStartOfDay(zoneId).toInstant().minusNanos(1);
    }
}
