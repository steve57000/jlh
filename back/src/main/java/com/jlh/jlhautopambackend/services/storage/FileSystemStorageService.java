package com.jlh.jlhautopambackend.services.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileSystemStorageService implements FileStorageService {

    private final Path rootLocation;

    public FileSystemStorageService(
            @Value("${app.upload-dir:/var/www/promo}") String root
    ) {
        this.rootLocation = Paths.get(root).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier de stockage", e);
        }
    }

    @Override
    public String store(MultipartFile file, String subfolder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide ou manquant.");
        }

        Path targetDir = StringUtils.hasText(subfolder)
                ? rootLocation.resolve(subfolder).normalize()
                : rootLocation;
        Files.createDirectories(targetDir);

        String original = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "file";
        String cleanName = StringUtils.cleanPath(original);
        String unique = UUID.randomUUID() + "_" + cleanName;

        Path targetFile = targetDir.resolve(unique).normalize();

        // Safety: ensure targetFile is under targetDir (prevent path traversal)
        if (!targetFile.startsWith(targetDir)) {
            throw new IOException("Chemin de destination invalide");
        }

        Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path, e.g. "documents/uuid_name.pdf"
        return (StringUtils.hasText(subfolder) ? subfolder + "/" : "") + unique;
    }

    @Override
    public Path load(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            throw new IllegalArgumentException("relativePath manquant");
        }
        Path resolved = rootLocation.resolve(relativePath).normalize();
        if (!resolved.startsWith(rootLocation)) {
            throw new IllegalArgumentException("Chemin invalide");
        }
        return resolved;
    }

    @Override
    public Resource loadAsResource(String relativePath) {
        Path p = load(relativePath);
        if (!Files.exists(p) || !Files.isReadable(p)) {
            throw new RuntimeException("Fichier introuvable: " + relativePath);
        }
        return new PathResource(p);
    }

    @Override
    public void delete(String relativePath) throws IOException {
        Path p = load(relativePath);
        if (Files.exists(p)) Files.delete(p);
    }
}
