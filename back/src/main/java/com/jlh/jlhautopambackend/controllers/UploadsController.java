package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.services.storage.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;

@RestController
public class UploadsController {

    private final FileStorageService storageService;

    public UploadsController(FileStorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/uploads/{*path}")
    public ResponseEntity<Resource> serveUpload(@PathVariable("path") String path) throws IOException {
        if (!StringUtils.hasText(path)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = storageService.loadAsResource(path);
        String contentType = Files.probeContentType(resource.getFile().toPath());
        MediaType mediaType = StringUtils.hasText(contentType)
                ? MediaType.parseMediaType(contentType)
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }
}
