package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.ClientDocumentDto;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.services.DemandeDocumentService;
import com.jlh.jlhautopambackend.services.support.AuthenticatedClientResolver;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/demandes/mes-documents")
@PreAuthorize("hasRole('CLIENT')")
public class ClientDocumentController {
    private final DemandeDocumentService documentService;
    private final AuthenticatedClientResolver clientResolver;

    public ClientDocumentController(DemandeDocumentService documentService,
                                    AuthenticatedClientResolver clientResolver) {
        this.documentService = documentService;
        this.clientResolver = clientResolver;
    }

    @GetMapping
    public List<ClientDocumentDto> list(Authentication auth) {
        Client client = clientResolver.requireCurrentClient(auth);
        return documentService.listClientDocuments(client.getIdClient());
    }
}
