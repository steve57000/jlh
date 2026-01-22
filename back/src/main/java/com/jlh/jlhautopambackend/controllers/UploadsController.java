package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.services.storage.FileStorageService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UploadsController {

    private final FileStorageService storageService;
    private final boolean staticFallbackEnabled;

    public UploadsController(
            FileStorageService storageService,
            Environment environment
    ) {
        this.storageService = storageService;
        this.staticFallbackEnabled = resolveStaticFallback(environment);
    }

    @GetMapping("/uploads/{*path}")
    public ResponseEntity<Resource> serveUpload(@PathVariable("path") String path) {
        if (!StringUtils.hasText(path)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = resolveResource(path);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }

    private Resource resolveResource(String path) {
        try {
            return storageService.loadAsResource(path);
        } catch (RuntimeException ex) {
            if (!staticFallbackEnabled) {
                return null;
            }
        }

        Resource classpathResource = new ClassPathResource("static/uploads/" + path);
        return classpathResource.exists() ? classpathResource : null;
    }

    private static boolean resolveStaticFallback(Environment environment) {
        if (environment == null) {
            return false;
        }
        String flag = environment.getProperty("app.uploads.static-fallback", "false");
        if (Boolean.parseBoolean(flag)) {
            return true;
        }
        for (String profile : environment.getActiveProfiles()) {
            if ("demo".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }
}
