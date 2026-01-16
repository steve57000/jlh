package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ClientDocumentDto;
import com.jlh.jlhautopambackend.dto.DemandeDocumentDownload;
import com.jlh.jlhautopambackend.dto.DemandeDocumentDto;
import com.jlh.jlhautopambackend.dto.DemandeTimelineRequest;
import com.jlh.jlhautopambackend.dto.RendezVousResponse;
import com.jlh.jlhautopambackend.dto.StatutDemandeDto;
import com.jlh.jlhautopambackend.dto.TypeDemandeDto;
import com.jlh.jlhautopambackend.mapper.RendezVousMapper;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.DemandeDocument;
import com.jlh.jlhautopambackend.modeles.DemandeTimelineType;
import com.jlh.jlhautopambackend.repository.DemandeDocumentRepository;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import com.jlh.jlhautopambackend.services.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DemandeDocumentServiceImpl implements DemandeDocumentService {

    private final DemandeRepository demandeRepo;
    private final DemandeDocumentRepository documentRepo;
    private final FileStorageService storageService;
    private final DemandeTimelineService timelineService;
    private final RendezVousMapper rendezVousMapper;

    public DemandeDocumentServiceImpl(
            DemandeRepository demandeRepo,
            DemandeDocumentRepository documentRepo,
            FileStorageService storageService,
            DemandeTimelineService timelineService,
            RendezVousMapper rendezVousMapper
    ) {
        this.demandeRepo = demandeRepo;
        this.documentRepo = documentRepo;
        this.storageService = storageService;
        this.timelineService = timelineService;
        this.rendezVousMapper = rendezVousMapper;
    }

    @Override
    public DemandeDocumentDto addDocument(Integer demandeId,
                                          MultipartFile file,
                                          String creePar,
                                          String creeParRole) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide ou manquant.");
        }

        Demande demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + demandeId));

        // store file -> returns relative path, e.g. "documents/uuid.pdf"
        String storedPath = storageService.store(file, "documents");

        DemandeDocument doc = DemandeDocument.builder()
                .demande(demande)
                .nomFichier(file.getOriginalFilename())
                .typeContenu(file.getContentType())
                .tailleOctets(file.getSize())
                .visibleClient(true)
                .creePar(creePar)
                .creeParRole(creeParRole)
                .creeLe(Instant.now())
                .urlPrivate(storedPath)
                .build();

        DemandeDocument saved = documentRepo.save(doc);

        // Log timeline (keep COMMENTAIRE if no DOCUMENT type supported)
        DemandeTimelineRequest tlReq = DemandeTimelineRequest.builder()
                .type(DemandeTimelineType.DOCUMENT)     // now a true DOCUMENT event
                .commentaire("Document ajouté : " + saved.getNomFichier())
                .visibleClient(true)
                .documentId(saved.getIdDocument())
                .documentNom(saved.getNomFichier())
                .build();

        timelineService.logAdminEvent(demandeId, tlReq, creePar);

        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeDocumentDto> listDocuments(Integer demandeId) {
        return documentRepo.findByDemande_IdDemandeOrderByCreeLeDesc(demandeId)
                .stream()
                .map(doc -> toDtoWithUrl(doc, demandeId, true))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DemandeDocumentDownload> loadDocument(Integer demandeId, Long documentId) {
        return documentRepo.findByIdDocumentAndDemande_IdDemande(documentId, demandeId)
                .map(doc -> DemandeDocumentDownload.builder()
                        .idDocument(doc.getIdDocument())
                        .demandeId(demandeId)
                        .nomFichier(doc.getNomFichier())
                        .urlPrivate(doc.getUrlPrivate())       // chemin relatif
                        .typeContenu(doc.getTypeContenu())
                        .tailleOctets(doc.getTailleOctets())
                        .visibleClient(doc.isVisibleClient())
                        .creePar(doc.getCreePar())
                        .creeParRole(doc.getCreeParRole())
                        .creeLe(doc.getCreeLe())
                        .build());
    }

    @Override
    public boolean deleteDocument(Integer demandeId, Long documentId) {
        Optional<DemandeDocument> opt = documentRepo.findByIdDocumentAndDemande_IdDemande(documentId, demandeId);
        opt.ifPresent(documentRepo::delete);
        return opt.isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOwnedByClient(Integer demandeId, Integer clientId) {
        return demandeRepo.existsByIdDemandeAndClient_IdClient(demandeId, clientId);
    }

    private DemandeDocumentDto toDto(DemandeDocument d) {
        return DemandeDocumentDto.builder()
                .idDocument(d.getIdDocument())
                .nomFichier(d.getNomFichier())
                // do NOT expose d.getUrlPrivate() here
                .typeContenu(d.getTypeContenu())
                .tailleOctets(d.getTailleOctets())
                .visibleClient(d.isVisibleClient())
                .creePar(d.getCreePar())
                .creeParRole(d.getCreeParRole())
                .creeLe(d.getCreeLe())
                .build();
    }

    private DemandeDocumentDto toDtoWithUrl(DemandeDocument d, Integer demandeId, boolean isAdmin) {
        DemandeDocumentDto dto = toDto(d);

        if (isAdmin) {
            dto.setDownloadUrl("/api/demandes/" + demandeId + "/documents/" + d.getIdDocument());
        } else {
            dto.setDownloadUrl("/api/demandes/" + demandeId + "/documents/client/" + d.getIdDocument());
        }

        return dto;
    }

    @Override
    public List<DemandeDocumentDto> listDocumentsForClient(Integer demandeId) {
        return documentRepo.findByDemande_IdDemandeOrderByCreeLeDesc(demandeId)
                .stream()
                .map(doc -> toDtoWithUrl(doc, demandeId, false)) // CLIENT
                .filter(DemandeDocumentDto::isVisibleClient)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDocumentDto> listClientDocuments(Integer clientId) {
        if (clientId == null) {
            return List.of();
        }
        return documentRepo.findByDemande_Client_IdClientOrderByCreeLeDesc(clientId)
                .stream()
                .filter(doc -> doc != null && doc.isVisibleClient())
                .map(this::toClientDocumentDto)
                .toList();
    }

    private ClientDocumentDto toClientDocumentDto(DemandeDocument doc) {
        Demande demande = doc.getDemande();
        TypeDemandeDto typeDemande = null;
        StatutDemandeDto statutDemande = null;
        RendezVousResponse rendezVous = null;
        if (demande != null) {
            if (demande.getTypeDemande() != null) {
                typeDemande = new TypeDemandeDto(demande.getTypeDemande().getCodeType(),
                        demande.getTypeDemande().getLibelle());
            }
            if (demande.getStatutDemande() != null) {
                statutDemande = new StatutDemandeDto(demande.getStatutDemande().getCodeStatut(),
                        demande.getStatutDemande().getLibelle());
            }
            if (demande.getRendezVous() != null) {
                rendezVous = rendezVousMapper.toResponse(demande.getRendezVous());
            }
        }
        return ClientDocumentDto.builder()
                .demandeId(demande != null ? demande.getIdDemande() : null)
                .dateDemande(demande != null ? demande.getDateDemande() : null)
                .typeDemande(typeDemande)
                .statutDemande(statutDemande)
                .rendezVous(rendezVous)
                .document(toDto(doc))
                .build();
    }

}
