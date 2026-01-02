package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.DemandeDocumentDownload;
import com.jlh.jlhautopambackend.dto.DemandeDocumentDto;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.services.DemandeDocumentService;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import com.jlh.jlhautopambackend.services.storage.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/demandes/{demandeId}/documents")
public class DemandeDocumentController {

    private final DemandeDocumentService documentService;
    private final ClientRepository clientRepository;
    private final FileStorageService storageService;

    public DemandeDocumentController(DemandeDocumentService documentService,
                                     ClientRepository clientRepository,
                                     FileStorageService storageService) {
        this.documentService = documentService;
        this.clientRepository = clientRepository;
        this.storageService = storageService;
    }

    // ---------------- ADMIN - upload / list / delete ----------------

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DemandeDocumentDto> upload(@PathVariable Integer demandeId,
                                                     @RequestParam("file") MultipartFile file,
                                                     Authentication authentication) {
        try {
            String email = authentication != null ? authentication.getName() : null;
            String role = resolveRole(authentication);

            DemandeDocumentDto created = documentService.addDocument(demandeId, file, email, role);

            URI location = URI.create(String.format("/api/demandes/%d/documents/%d",
                    demandeId, created.getIdDocument()));

            return ResponseEntity.created(location).body(created);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors de l'enregistrement du fichier.", e);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DemandeDocumentDto>> list(@PathVariable Integer demandeId) {
        List<DemandeDocumentDto> docs = documentService.listDocuments(demandeId);
        return ResponseEntity.ok(docs);
    }

    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer demandeId,
                                       @PathVariable Long documentId) {
        boolean deleted = documentService.deleteDocument(demandeId, documentId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // ---------------- ADMIN - download/stream secure ----------------
    /**
     * Retourne le fichier via le backend (stream) si le document est privé.
     * Si le document contient une urlPublic externe, on redirige vers celle-ci.
     * Auth : ADMIN
     */
    @GetMapping("/{documentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDocumentForAdmin(@PathVariable Integer demandeId,
                                                 @PathVariable Long documentId) {
        Optional<DemandeDocumentDownload> downloadOpt =
                documentService.loadDocument(demandeId, documentId);

        if (downloadOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        DemandeDocumentDownload doc = downloadOpt.get();

        // Sinon on doit streamer le fichier stocké (urlPrivate contient le chemin relatif en storage)
        if (!StringUtils.hasText(doc.getUrlPrivate())) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = storageService.loadAsResource(doc.getUrlPrivate());
            String filename = doc.getNomFichier() != null ? doc.getNomFichier() : resource.getFilename();
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (StringUtils.hasText(doc.getTypeContenu())) {
                try {
                    mediaType = MediaType.parseMediaType(doc.getTypeContenu());
                } catch (Exception ignored) { }
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(filename).build().toString())
                    .body(resource);

        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossible de lire le fichier.", ex);
        }
    }

    // ---------------- CLIENT - list & download (seulement les visibles et possédés) ----------------

    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<DemandeDocumentDto>> listForClient(@PathVariable Integer demandeId,
                                                                  Authentication authentication) {
        Client client = requireCurrentClient(authentication);

        boolean owned = documentService.isOwnedByClient(demandeId, client.getIdClient());
        if (!owned) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<DemandeDocumentDto> allDocs = documentService.listDocumentsForClient(demandeId);
        List<DemandeDocumentDto> visibleDocs = allDocs.stream()
                .filter(DemandeDocumentDto::isVisibleClient)
                .toList();

        return ResponseEntity.ok(visibleDocs);
    }

    @GetMapping("/client/{documentId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> getDocumentForClient(@PathVariable Integer demandeId,
                                                  @PathVariable Long documentId,
                                                  Authentication authentication) {
        Client client = requireCurrentClient(authentication);

        boolean owned = documentService.isOwnedByClient(demandeId, client.getIdClient());
        if (!owned) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<DemandeDocumentDownload> downloadOpt = documentService.loadDocument(demandeId, documentId);
        if (downloadOpt.isEmpty()) return ResponseEntity.notFound().build();
        DemandeDocumentDownload doc = downloadOpt.get();

        if (!doc.isVisibleClient()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!StringUtils.hasText(doc.getUrlPrivate())) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = storageService.loadAsResource(doc.getUrlPrivate());
            String filename = doc.getNomFichier() != null ? doc.getNomFichier() : resource.getFilename();
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (StringUtils.hasText(doc.getTypeContenu())) {
                try {
                    mediaType = MediaType.parseMediaType(doc.getTypeContenu());
                } catch (Exception ignored) { }
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(filename).build().toString())
                    .body(resource);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossible de lire le fichier.", ex);
        }
    }

    // -------- helpers --------
    private String resolveRole(Authentication authentication) {
        if (authentication == null) return null;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring("ROLE_".length()))
                .findFirst().orElse(null);
    }

    private Client requireCurrentClient(Authentication authentication) {
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié.");
        }
        String email = authentication.getName();
        return clientRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Aucun client associé à l'utilisateur connecté."));
    }
}
