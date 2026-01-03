package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.*;
import com.jlh.jlhautopambackend.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DemandeMapperTest {

    private DemandeMapper mapper;
    private UserService userService;

    @BeforeEach
    void setUp() {

        // Client mapper avec passwordEncoder mock
        ClientMapperImpl clientMapper = new ClientMapperImpl();
        clientMapper.setPasswordEncoder(new PasswordEncoder() {
            @Override public String encode(CharSequence rawPassword) {
                return rawPassword == null ? null : rawPassword.toString();
            }
            @Override public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword != null && encodedPassword != null && encodedPassword.contentEquals(rawPassword);
            }
            @Override public boolean upgradeEncoding(String encodedPassword) { return false; }
        });

        // Timeline mapper (sans userService ici, il sera donné à l’appel)
        DemandeTimelineMapperImpl timelineMapper = new DemandeTimelineMapperImpl();

        // Mapper principal
        mapper = new DemandeMapperImpl(timelineMapper, clientMapper);

        // Mock UserService
        userService = mock(UserService.class);
        when(userService.getFirstnameFromEmail(anyString())).thenReturn("Alice");
    }

    @Test
    void toEntity_shouldMapDateAndIgnoreIdsAndRelations() {
        Instant now = Instant.parse("2025-07-14T12:00:00Z");
        DemandeRequest req = DemandeRequest.builder()
                .dateDemande(now)
                .clientId(42)
                .codeType("TYP")
                .codeStatut("ST1")
                .build();

        Demande ent = mapper.toEntity(req);

        // idDemande ignoré par le mapping
        assertNull(ent.getIdDemande());
        // date bien mappée
        assertEquals(now, ent.getDateDemande());
        // relations non hydratées par toEntity()
        assertNull(ent.getClient());
        assertNull(ent.getTypeDemande());
        assertNull(ent.getStatutDemande());
        assertTrue(ent.getServices() == null || ent.getServices().isEmpty());
        assertTrue(ent.getDocuments() == null || ent.getDocuments().isEmpty());
    }

    @Test
    void toResponse_shouldMapAllFields_includingClientAndServices() {
        Instant dt = Instant.parse("2025-06-01T08:30:00Z");

        Client client = Client.builder()
                .idClient(7)
                .nom("Durand")
                .prenom("Alice")
                .email("alice@example.com")
                .build();

        TypeDemande td = TypeDemande.builder()
                .codeType("T1")
                .libelle("Type 1")
                .build();

        StatutDemande sd = StatutDemande.builder()
                .codeStatut("S1")
                .libelle("Statut 1")
                .build();

        Service service = Service.builder()
                .idService(456)
                .libelle("Vidange")
                .quantiteMax(3)
                .prixUnitaire(BigDecimal.valueOf(59.90))
                .build();

        DemandeServiceKey key = new DemandeServiceKey(123, 456);
        DemandeService ds = DemandeService.builder()
                .id(key)
                .service(service)
                .quantite(2)
                .libelleService("Vidange")
                .descriptionService("Vidange moteur")
                .prixUnitaireService(BigDecimal.valueOf(59.90))
                .build();

        DemandeDocument document = DemandeDocument.builder()
                .idDocument(321L)
                .nomFichier("devis.pdf")
                .urlPrivate("https://cdn.example.com/docs/devis.pdf")
                .typeContenu("application/pdf")
                .tailleOctets(2048L)
                .visibleClient(true)
                .creePar("admin@test.fr")
                .creeParRole("ADMIN")
                .creeLe(Instant.parse("2025-06-01T08:31:00Z"))
                .build();

        Demande ent = Demande.builder()
                .idDemande(99)
                .dateDemande(dt)
                .client(client)
                .typeDemande(td)
                .statutDemande(sd)
                .services(Set.of(ds))
                .documents(Set.of(document))
                .build();

        document.setDemande(ent);

        DemandeResponse resp = mapper.toResponse(ent, userService);

        assertEquals(99, resp.getIdDemande());
        assertEquals(dt, resp.getDateDemande());

        // client
        assertNotNull(resp.getClient());
        assertEquals("Durand", resp.getClient().getNom());

        // documents
        assertEquals(1, resp.getDocuments().size());
        DemandeDocumentDto d = resp.getDocuments().get(0);
        assertEquals("devis.pdf", d.getNomFichier());
        assertEquals("ADMIN", d.getCreeParRole());
    }

    @Test
    void toDto_shouldProjectSimplifiedView() {
        Instant dt = Instant.parse("2025-06-01T08:30:00Z");
        Client client = Client.builder()
                .idClient(7)
                .nom("Durand")
                .prenom("Alice")
                .email("alice@example.com")
                .telephone("0102030405")
                .adresseLigne1("10 rue Victor")
                .adresseCodePostal("75000")
                .adresseVille("Paris")
                .build();

        TypeDemande td = TypeDemande.builder()
                .codeType("T1")
                .libelle("Type 1")
                .build();

        StatutDemande sd = StatutDemande.builder()
                .codeStatut("S1")
                .libelle("Statut 1")
                .build();

        DemandeService serviceLine = DemandeService.builder()
                .id(new DemandeServiceKey(99, 456))
                .quantite(2)
                .libelleService("Vidange")
                .descriptionService("Vidange moteur")
                .prixUnitaireService(BigDecimal.valueOf(59.90))
                .build();

        Demande demande = Demande.builder()
                .idDemande(99)
                .dateDemande(dt)
                .client(client)
                .typeDemande(td)
                .statutDemande(sd)
                .services(Set.of(serviceLine))
                .build();
        serviceLine.setDemande(demande);

        DemandeDto dto = mapper.toDto(demande);

        assertEquals(99, dto.getIdDemande());
        assertEquals(dt, dto.getDateSoumission());
        assertEquals("T1", dto.getCodeType());
        assertEquals("Type 1", dto.getTypeLibelle());
        assertEquals("S1", dto.getCodeStatut());
        assertEquals("Statut 1", dto.getStatutLibelle());

        assertNotNull(dto.getClient());
        assertEquals(7, dto.getClient().getIdClient());
        assertEquals("Durand", dto.getClient().getNom());
        assertEquals("10 rue Victor 75000 Paris", dto.getClient().getAdresse());

        assertNotNull(dto.getServices());
        assertEquals(1, dto.getServices().size());
        DemandeServiceDto svc = dto.getServices().get(0);
        assertEquals(99, svc.getIdDemande());
        assertEquals(456, svc.getIdService());
        assertEquals("Vidange", svc.getLibelle());
        assertEquals("Vidange", svc.getLibelleService());
        assertEquals("Vidange moteur", svc.getDescription());
        assertEquals(BigDecimal.valueOf(59.90), svc.getPrixUnitaire());
        assertEquals(BigDecimal.valueOf(59.90), svc.getPrixUnitaireService());
    }
}
