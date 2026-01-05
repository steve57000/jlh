package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ServiceIconRequest;
import com.jlh.jlhautopambackend.dto.ServiceIconResponse;
import com.jlh.jlhautopambackend.modeles.ServiceIcon;
import com.jlh.jlhautopambackend.repository.ServiceIconRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ServiceIconServiceImpl implements ServiceIconService {
    private final ServiceIconRepository repo;

    public ServiceIconServiceImpl(ServiceIconRepository repo) {
        this.repo = repo;
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
    public boolean delete(Integer id) {
        if (!repo.existsById(id)) {
            return false;
        }
        repo.deleteById(id);
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

    private ServiceIconResponse toResponse(ServiceIcon icon) {
        return ServiceIconResponse.builder()
                .idIcon(icon.getIdIcon())
                .url(icon.getUrl())
                .label(icon.getLabel())
                .build();
    }
}
