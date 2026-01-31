package com.jlh.jlhautopambackend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.jlh.jlhautopambackend.dto.AvisServiceResponse;
import com.jlh.jlhautopambackend.modeles.AvisService;
import com.jlh.jlhautopambackend.modeles.AvisServiceStatut;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.Service;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AvisServiceMapperTest {

    @Test
    void toResponse_mapsAvisAndClientDetails() {
        AvisServiceMapper mapper = new AvisServiceMapper();

        Client client = Client.builder()
                .idClient(10)
                .nom("Durand")
                .prenom("Alice")
                .email("alice@test.fr")
                .telephone("0600000000")
                .adresseLigne1("12 rue Victor Hugo")
                .adresseCodePostal("75003")
                .adresseVille("Paris")
                .build();
        Demande demande = Demande.builder()
                .idDemande(25)
                .build();
        Service service = Service.builder()
                .idService(7)
                .libelle("Freinage")
                .build();
        Instant createdAt = Instant.parse("2025-02-21T10:00:00Z");

        AvisService avis = AvisService.builder()
                .idAvis(4L)
                .demande(demande)
                .service(service)
                .client(client)
                .note(5)
                .commentaire("Service rapide et clair.")
                .creeLe(createdAt)
                .statut(AvisServiceStatut.PENDING)
                .motifRefus(null)
                .build();

        AvisServiceResponse response = mapper.toResponse(avis);

        assertNotNull(response);
        assertEquals(4L, response.getIdAvis());
        assertEquals(25, response.getDemandeId());
        assertEquals(7, response.getServiceId());
        assertEquals("Freinage", response.getServiceLibelle());
        assertEquals(10, response.getClientId());
        assertEquals("Alice Durand", response.getClientNomPrenom());
        assertEquals(5, response.getNote());
        assertEquals("Service rapide et clair.", response.getCommentaire());
        assertEquals(createdAt, response.getCreeLe());
        assertEquals("PENDING", response.getStatut());
        assertNotNull(response.getClient());
        assertEquals("Durand", response.getClient().getNom());
        assertEquals("Alice", response.getClient().getPrenom());
        assertEquals("alice@test.fr", response.getClient().getEmail());
    }
}
