package com.jlh.jlhautopambackend.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientResponseTest {

    @Test
    @DisplayName("Builder should set all fields and getters should retrieve them")
    void builderAndGetters() {
        ClientResponse resp = ClientResponse.builder()
                .idClient(42)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .telephone("0123456789")
                .adresseLigne1("1 rue A")
                .adresseLigne2("2 rue B")
                .codePostal("57000")
                .ville("Metz")
                .build();

        assertEquals(42, resp.getIdClient());
        assertEquals("Dupont", resp.getNom());
        assertEquals("Jean", resp.getPrenom());
        assertEquals("jean.dupont@example.com", resp.getEmail());
        assertEquals("0123456789", resp.getTelephone());
        assertEquals("1 rue A", resp.getAdresseLigne1());
        assertEquals("2 rue B", resp.getAdresseLigne1());
        assertEquals("57000", resp.getCodePostal());
        assertEquals("Metz", resp.getVille());
    }

    @Test
    @DisplayName("Setters should modify fields and getters should retrieve updated values")
    void settersAndGetters() {
        ClientResponse resp = new ClientResponse();
        resp.setIdClient(99);
        resp.setNom("Martin");
        resp.setPrenom("Marie");
        resp.setEmail("marie.martin@example.com");
        resp.setTelephone("0987654321");
        resp.setAdresseLigne1("2 avenue B");
        resp.setAdresseLigne2("1 avenue C");
        resp.setCodePostal("57000");
        resp.setVille("Metz");

        assertEquals(99, resp.getIdClient());
        assertEquals("Martin", resp.getNom());
        assertEquals("Marie", resp.getPrenom());
        assertEquals("marie.martin@example.com", resp.getEmail());
        assertEquals("0987654321", resp.getTelephone());
        assertEquals("2 avenue B", resp.getAdresseLigne1());
        assertEquals("1 avenue C", resp.getAdresseLigne1());
        assertEquals("57000", resp.getCodePostal());
        assertEquals("Metz", resp.getVille());
    }

    @Test
    @DisplayName("equals and hashCode should be consistent")
    void equalsAndHashCode() {
        ClientResponse a = ClientResponse.builder()
                .idClient(1).nom("A").prenom("B")
                .email("a@b.com").telephone("111").adresseLigne1("Z").adresseLigne2("I").codePostal("57").ville("Metz").build();
        ClientResponse b = ClientResponse.builder()
                .idClient(1).nom("A").prenom("B")
                .email("a@b.com").telephone("111").adresseLigne1("Z").adresseLigne2("I").codePostal("57").ville("Metz").build();
        ClientResponse c = ClientResponse.builder()
                .idClient(2).nom("A").prenom("B")
                .email("a@b.com").telephone("111")
                .adresseLigne1("Z").adresseLigne2("I")
                .codePostal("57").ville("Metz")
                .build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    @DisplayName("toString should contain all fields")
    void toStringContainsAllFields() {
        ClientResponse resp = ClientResponse.builder()
                .idClient(7).nom("T").prenom("U")
                .email("t@u.com").telephone("000")
                .adresseLigne1("I").adresseLigne2("J")
                .codePostal("57").ville("Metz")
                .build();
        String s = resp.toString();
        assertTrue(s.contains("idClient=7"));
        assertTrue(s.contains("nom=T"));
        assertTrue(s.contains("prenom=U"));
        assertTrue(s.contains("email=t@u.com"));
        assertTrue(s.contains("telephone=000"));
        assertTrue(s.contains("adresse=Addr"));
    }
}
