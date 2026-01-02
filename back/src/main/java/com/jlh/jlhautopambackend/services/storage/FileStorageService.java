package com.jlh.jlhautopambackend.services.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {

    /**
     * Store the given file in the given subfolder and return the relative path
     * (ex: "documents/uuid_name.pdf").
     */
    String store(MultipartFile file, String subfolder) throws IOException;

    /**
     * Return an absolute Path to the stored file.
     */
    Path load(String relativePath);

    /**
     * Return a Spring Resource for streaming (throws runtime exception if not found).
     */
    Resource loadAsResource(String relativePath);

    /**
     * Delete stored file (if exists).
     */
    void delete(String relativePath) throws IOException;
}
