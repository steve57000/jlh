package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.ClientRequest;
import com.jlh.jlhautopambackend.dto.ClientResponse;
import com.jlh.jlhautopambackend.modeles.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class ClientMapperTest {
    private ClientMapper mapper;

    @BeforeEach
    void setUp() {
        ClientMapperImpl impl = new ClientMapperImpl();
        impl.setPasswordEncoder(new StubPasswordEncoder());
        mapper = impl;
    }

    private static class StubPasswordEncoder implements PasswordEncoder {
        @Override
        public String encode(CharSequence rawPassword) {
            return "ENC(" + rawPassword + ")";
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return encode(rawPassword).equals(encodedPassword);
        }

        @Override
        public boolean upgradeEncoding(String encodedPassword) {
            return false;
        }
    }

    @Test
    void shouldMapRequestToEntityAndBack() {
        // GIVEN: un DTO complet (tous les champs requis)
        ClientRequest dto = ClientRequest.builder()
                .nom("Dupont")
                .prenom("Jean")
                .email("jean@dupont.fr")
                .motDePasse("secret123")         // ajouté
                .immatriculation("AA-123-AA")    // ajouté
                .vehiculeMarque("Renault")
                .vehiculeModele("Megane")
                .telephone("0102030405")
                .adresseLigne1("1 bis ter")
                .adresseLigne2("2 bis ter")
                .codePostal("654")
                .ville("Metz")
                .build();

        // WHEN: mapping vers l'entité
        Client ent = mapper.toEntity(dto);

        // THEN: id ignoré et champs copiés
        assertNull(ent.getIdClient());
        assertEquals("Dupont", ent.getNom());
        assertEquals("Jean", ent.getPrenom());
        assertEquals("jean@dupont.fr", ent.getEmail());
        assertEquals("AA-123-AA", ent.getImmatriculation());
        assertEquals("Renault", ent.getVehiculeMarque());
        assertEquals("Megane", ent.getVehiculeModele());
        assertEquals("0102030405", ent.getTelephone());
        assertEquals("1 bis ter", ent.getAdresseLigne1());
        assertEquals("2 bis ter", ent.getAdresseLigne2());
        assertEquals("654", ent.getAdresseCodePostal());
        assertEquals("Metz", ent.getAdresseVille());
        assertEquals("ENC(secret123)", ent.getMotDePasse());
        // selon ton mapper, tu peux hasher/saisir le mot de passe ailleurs,
        // ici on vérifie juste que le champ est bien passé si mappé tel quel :
        // assertEquals("secret123", ent.getMotDePasse());

        // WHEN: mapping retour vers la réponse
        ent.setIdClient(7);
        ClientResponse res = mapper.toResponse(ent);

        // THEN: réponse cohérente
        assertEquals(7, res.getIdClient());
        assertEquals("Dupont", res.getNom());
        assertEquals("Jean", res.getPrenom());
        assertEquals("jean@dupont.fr", res.getEmail());
        assertEquals("0102030405", res.getTelephone());
        assertEquals("AA-123-AA", res.getImmatriculation());
        assertEquals("Renault", res.getVehiculeMarque());
        assertEquals("Megane", res.getVehiculeModele());
        assertEquals("1 bis ter", res.getAdresseLigne1());
        assertEquals("2 bis ter", res.getAdresseLigne2());
        assertEquals("654", res.getCodePostal());
        assertEquals("Metz", res.getVille());
    }
}
