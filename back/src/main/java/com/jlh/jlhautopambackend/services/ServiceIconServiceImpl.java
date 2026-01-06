package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ServiceIconRequest;
import com.jlh.jlhautopambackend.dto.ServiceIconResponse;
import com.jlh.jlhautopambackend.modeles.ServiceIcon;
import com.jlh.jlhautopambackend.repository.ServiceIconRepository;
import com.jlh.jlhautopambackend.services.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ServiceIconServiceImpl implements ServiceIconService {
    private final ServiceIconRepository repo;
    private final FileStorageService storageService;
    private final String filesBaseUrl;

    public ServiceIconServiceImpl(
            ServiceIconRepository repo,
            FileStorageService storageService,
            @Value("${app.files.base-url}") String filesBaseUrl
    ) {
        this.repo = repo;
        this.storageService = storageService;
        this.filesBaseUrl = StringUtils.trimTrailingCharacter(filesBaseUrl, '/');
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceIconResponse> findAll() {
        return repo.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Optional<ServiceIconResponse> create(ServiceIconRequest request) {
        String url = sanitizeUrl(request.getUrl());
        if (!StringUtils.hasText(url)) {
            return Optional.empty();
        }
        Optional<ServiceIcon> existing = repo.findByUrl(url);
        if (existing.isPresent()) {
            return Optional.of(toResponse(existing.get()));
        }
        ServiceIcon saved = repo.save(ServiceIcon.builder()
                .url(url)
                .label(StringUtils.hasText(request.getLabel()) ? request.getLabel().trim() : null)
                .build());
        return Optional.of(toResponse(saved));
    }

    @Override
    public Optional<ServiceIconResponse> createFromFile(MultipartFile file, String label) {
        if (file == null || file.isEmpty()) {
            return Optional.empty();
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Le fichier doit être une image.");
        }
        String storedPath;
        try {
            storedPath = storageService.store(file, "icons");
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'enregistrer l'image.", ex);
        }
        String url = buildPublicUrl(storedPath);
        Optional<ServiceIcon> existing = repo.findByUrl(url);
        if (existing.isPresent()) {
            return Optional.of(toResponse(existing.get()));
        }
        ServiceIcon saved = repo.save(ServiceIcon.builder()
                .url(url)
                .label(StringUtils.hasText(label) ? label.trim() : null)
                .build());
        return Optional.of(toResponse(saved));
    }

    @Override
    public Optional<ServiceIconResponse> update(Integer id, ServiceIconRequest request) {
        Optional<ServiceIcon> existing = repo.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        String url = sanitizeUrl(request.getUrl());
        if (!StringUtils.hasText(url)) {
            return Optional.empty();
        }
        Optional<ServiceIcon> existingByUrl = repo.findByUrl(url);
        if (existingByUrl.isPresent() && !existingByUrl.get().getIdIcon().equals(id)) {
            return Optional.empty();
        }
        ServiceIcon icon = existing.get();
        icon.setUrl(url);
        icon.setLabel(StringUtils.hasText(request.getLabel()) ? request.getLabel().trim() : null);
        ServiceIcon saved = repo.save(icon);
        return Optional.of(toResponse(saved));
    }

    @Override
    public Optional<ServiceIconResponse> updateFromFile(Integer id, MultipartFile file, String label) {
        Optional<ServiceIcon> existing = repo.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        if (file == null || file.isEmpty()) {
            return Optional.empty();
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Le fichier doit être une image.");
        }
        String storedPath;
        try {
            storedPath = storageService.store(file, "icons");
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'enregistrer l'image.", ex);
        }
        String url = buildPublicUrl(storedPath);
        Optional<ServiceIcon> existingByUrl = repo.findByUrl(url);
        if (existingByUrl.isPresent() && !existingByUrl.get().getIdIcon().equals(id)) {
            return Optional.empty();
        }
        ServiceIcon icon = existing.get();
        icon.setUrl(url);
        icon.setLabel(StringUtils.hasText(label) ? label.trim() : null);
        ServiceIcon saved = repo.save(icon);
        return Optional.of(toResponse(saved));
    }

    @Override
    public boolean delete(Integer id) {
        Optional<ServiceIcon> existing = repo.findById(id);
        if (existing.isEmpty()) {
            return false;
        }
        String storedPath = resolveStoredPath(existing.get().getUrl());
        if (StringUtils.hasText(storedPath)) {
            try {
                storageService.delete(storedPath);
            } catch (IOException ignored) {
            }
        }
        repo.delete(existing.get());
        return true;
    }

    @Override
    public void ensureIconExists(String url) {
        String cleaned = sanitizeUrl(url);
        if (!StringUtils.hasText(cleaned)) {
            return;
        }
        repo.findByUrl(cleaned).orElseGet(() -> repo.save(ServiceIcon.builder()
                .url(cleaned)
                .label(null)
                .build()));
    }

    private String sanitizeUrl(String url) {
        return StringUtils.hasText(url) ? url.trim() : "";
    }

    private String buildPublicUrl(String storedPath) {
        String cleanPath = storedPath.startsWith("/") ? storedPath.substring(1) : storedPath;
        return filesBaseUrl + "/" + cleanPath;
    }

    private String resolveStoredPath(String url) {
        String cleaned = sanitizeUrl(url);
        if (!StringUtils.hasText(cleaned) || !cleaned.startsWith(filesBaseUrl)) {
            return "";
        }
        String relative = cleaned.substring(filesBaseUrl.length());
        int queryIndex = relative.indexOf('?');
        if (queryIndex >= 0) {
            relative = relative.substring(0, queryIndex);
        }
        int fragmentIndex = relative.indexOf('#');
        if (fragmentIndex >= 0) {
            relative = relative.substring(0, fragmentIndex);
        }
        while (relative.startsWith("/") || relative.startsWith("\\")) {
            relative = relative.substring(1);
        }
        String normalized = StringUtils.cleanPath(relative);
        if (!normalized.startsWith("icons/") && !normalized.equals("icons")) {
            return "";
        }
        return normalized;
    }

    private ServiceIconResponse toResponse(ServiceIcon icon) {
        return ServiceIconResponse.builder()
                .idIcon(icon.getIdIcon())
                .url(icon.getUrl())
                .label(icon.getLabel())
                .build();
    }
}
