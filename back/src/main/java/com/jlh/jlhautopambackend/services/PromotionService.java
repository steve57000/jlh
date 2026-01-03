package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.PromotionRequest;
import com.jlh.jlhautopambackend.dto.PromotionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;

public interface PromotionService {
    List<PromotionResponse> findAll();
    Optional<PromotionResponse> findById(Integer id);

    /**
     * Crée une promotion avec un fichier optionnel (image ou PDF).
     * @throws IOException en cas d'erreur de stockage
     */
    PromotionResponse create(PromotionRequest request, MultipartFile file) throws IOException;

    /**
     * Met à jour une promotion avec un fichier optionnel.
     * @throws IOException en cas d'erreur de stockage
     */
    Optional<PromotionResponse> update(Integer id, PromotionRequest request, MultipartFile file) throws IOException;

    boolean delete(Integer id);

    /**
     * Surcharge sans MultipartFile pour compatibilité tests.
     * Capture IOException et renvoie une UncheckedIOException.
     */
    default PromotionResponse create(PromotionRequest request) {
        try {
            return create(request, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Surcharge sans MultipartFile pour compatibilité tests.
     * Capture IOException et renvoie une UncheckedIOException.
     */
    default Optional<PromotionResponse> update(Integer id, PromotionRequest request) {
        try {
            return update(id, request, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
