package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.AvisServiceResponse;
import com.jlh.jlhautopambackend.dto.ClientSummaryDto;
import com.jlh.jlhautopambackend.modeles.AvisService;
import com.jlh.jlhautopambackend.modeles.Client;
import org.springframework.stereotype.Component;

@Component
public class AvisServiceMapper {

    public AvisServiceResponse toResponse(AvisService avis) {
        if (avis == null) {
            return null;
        }
        Client client = avis.getClient();
        ClientSummaryDto clientSummary = null;
        if (client != null) {
            clientSummary = ClientSummaryDto.builder()
                    .idClient(client.getIdClient())
                    .nom(client.getNom())
                    .prenom(client.getPrenom())
                    .email(client.getEmail())
                    .telephone(client.getTelephone())
                    .adresseLigne1(client.getAdresseLigne1())
                    .adresseLigne2(client.getAdresseLigne2())
                    .adresseCodePostal(client.getAdresseCodePostal())
                    .adresseVille(client.getAdresseVille())
                    .build();
        }
        String serviceLibelle = avis.getService() != null ? avis.getService().getLibelle() : null;
        String clientNomPrenom = client != null ? (client.getPrenom() + " " + client.getNom()) : null;
        return AvisServiceResponse.builder()
                .idAvis(avis.getIdAvis())
                .demandeId(avis.getDemande() != null ? avis.getDemande().getIdDemande() : null)
                .serviceId(avis.getService() != null ? avis.getService().getIdService() : null)
                .serviceLibelle(serviceLibelle)
                .clientId(client != null ? client.getIdClient() : null)
                .clientNomPrenom(clientNomPrenom)
                .note(avis.getNote())
                .commentaire(avis.getCommentaire())
                .creeLe(avis.getCreeLe())
                .statut(avis.getStatut() != null ? avis.getStatut().name() : null)
                .motifRefus(avis.getMotifRefus())
                .client(clientSummary)
                .build();
    }
}
