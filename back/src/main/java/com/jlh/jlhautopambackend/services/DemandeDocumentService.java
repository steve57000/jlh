package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ClientDocumentDto;
import com.jlh.jlhautopambackend.dto.DemandeDocumentDownload;
import com.jlh.jlhautopambackend.dto.DemandeDocumentDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface DemandeDocumentService {

    /**
     * Ajoute un document à une demande.
     *
     * @param demandeId   identifiant de la demande
     * @param file        fichier uploadé
     * @param creePar     email de l'utilisateur qui crée le document (admin ou client)
     * @param creeParRole rôle logique ("ADMIN", "CLIENT", ...)
     */
    DemandeDocumentDto addDocument(Integer demandeId,
                                   MultipartFile file,
                                   String creePar,
                                   String creeParRole) throws IOException;

    List<DemandeDocumentDto> listDocuments(Integer demandeId);

    List<DemandeDocumentDto> listDocumentsForClient(Integer demandeId);

    List<ClientDocumentDto> listClientDocuments(Integer clientId);

    Optional<DemandeDocumentDownload> loadDocument(Integer demandeId, Long documentId);

    boolean deleteDocument(Integer demandeId, Long documentId);

    boolean isOwnedByClient(Integer demandeId, Integer clientId);

}
