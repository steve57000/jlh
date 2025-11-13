package com.jlh.jlhautopambackend.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    /** Sauvegarde le fichier et retourne le chemin relatif (ex. "promotions/abc.jpg") */
    String store(MultipartFile file) throws IOException;

    /** Supprime le fichier à partir de son chemin relatif */
    void delete(String path) throws IOException;
}
