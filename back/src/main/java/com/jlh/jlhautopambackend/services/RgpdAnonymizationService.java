package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.config.RgpdProperties;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.DemandeDocument;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import com.jlh.jlhautopambackend.repository.DemandeDocumentRepository;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import com.jlh.jlhautopambackend.repository.RendezVousRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RgpdAnonymizationService {
    private static final List<String> ACTIVE_STATUSES = List.of("Brouillon", "En_attente");
    private static final String ANONYMIZED_EMAIL_DOMAIN = "@example.invalid";
    private static final String ANONYMIZED_PHONE = "0000000000";
    private static final String ANONYMIZED_NAME = "Anonymisé";

    private final ClientRepository clientRepository;
    private final DemandeRepository demandeRepository;
    private final DemandeDocumentRepository documentRepository;
    private final RendezVousRepository rendezVousRepository;
    private final PasswordEncoder passwordEncoder;
    private final RgpdProperties rgpdProperties;

    public RgpdAnonymizationService(ClientRepository clientRepository,
                                    DemandeRepository demandeRepository,
                                    DemandeDocumentRepository documentRepository,
                                    RendezVousRepository rendezVousRepository,
                                    PasswordEncoder passwordEncoder,
                                    RgpdProperties rgpdProperties) {
        this.clientRepository = clientRepository;
        this.demandeRepository = demandeRepository;
        this.documentRepository = documentRepository;
        this.rendezVousRepository = rendezVousRepository;
        this.passwordEncoder = passwordEncoder;
        this.rgpdProperties = rgpdProperties;
    }

    public Optional<Client> anonymizeClient(Integer clientId, String actorEmail) {
        if (clientId == null) {
            return Optional.empty();
        }
        return clientRepository.findById(clientId).map(client -> {
            applyAnonymization(client, actorEmail);
            return clientRepository.save(client);
        });
    }

    public int anonymizeEligibleClients() {
        if (!rgpdProperties.isAnonymizationEnabled()) {
            return 0;
        }
        Instant cutoff = Instant.now().minus(rgpdProperties.getRetentionDays(), ChronoUnit.DAYS);
        int count = 0;
        for (Client client : clientRepository.findByAnonymizedFalse()) {
            if (client == null || client.getIdClient() == null) {
                continue;
            }
            if (!isEligible(client, cutoff)) {
                continue;
            }
            applyAnonymization(client, "AUTO_RGPD");
            clientRepository.save(client);
            count += 1;
        }
        return count;
    }

    @Scheduled(cron = "${rgpd.anonymizationCron:0 0 3 * * *}")
    public void anonymizeEligibleClientsJob() {
        anonymizeEligibleClients();
    }

    private boolean isEligible(Client client, Instant cutoff) {
        Integer clientId = client.getIdClient();
        Optional<Instant> lastDemande = demandeRepository.findLatestDateDemandeByClientId(clientId);
        if (lastDemande.isEmpty() || lastDemande.get().isAfter(cutoff)) {
            return false;
        }
        if (demandeRepository.existsByClient_IdClientAndStatutDemande_CodeStatutIn(clientId, ACTIVE_STATUSES)) {
            return false;
        }
        return rendezVousRepository.countUpcomingByClientId(clientId, Instant.now()) == 0;
    }

    private void applyAnonymization(Client client, String actorEmail) {
        if (client == null || client.isAnonymized()) {
            return;
        }
        Instant now = Instant.now();

        client.setNom(ANONYMIZED_NAME);
        client.setPrenom(null);
        client.setEmail(buildAnonymizedEmail(client.getIdClient()));
        client.setTelephone(ANONYMIZED_PHONE);
        client.setAdresseLigne1(null);
        client.setAdresseLigne2(null);
        client.setAdresseCodePostal(null);
        client.setAdresseVille(null);
        client.setEmailVerified(false);
        client.setEmailVerifiedAt(null);
        client.setMotDePasse(passwordEncoder.encode(UUID.randomUUID().toString()));
        client.setVehicule(null);
        client.setAnonymized(true);
        client.setAnonymizedAt(now);

        List<Demande> demandes = demandeRepository.findByClient_IdClient(client.getIdClient());
        for (Demande demande : demandes) {
            demande.setImmatriculation(null);
        }

        List<DemandeDocument> docs = documentRepository.findByDemande_Client_IdClientOrderByCreeLeDesc(client.getIdClient());
        for (DemandeDocument doc : docs) {
            if (doc.getIdDocument() != null) {
                doc.setNomFichier("document-" + doc.getIdDocument());
            }
            doc.setCreePar(actorEmail != null ? actorEmail : "ANONYMIZED");
            doc.setCreeParRole(null);
        }
    }

    private String buildAnonymizedEmail(Integer clientId) {
        return "anonymized+" + clientId + ANONYMIZED_EMAIL_DOMAIN;
    }
}
