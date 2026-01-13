package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ClientRequest;
import com.jlh.jlhautopambackend.dto.ClientResponse;
import com.jlh.jlhautopambackend.dto.ClientUpdateRequest;
import com.jlh.jlhautopambackend.mapper.ClientMapper;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository repository;
    private final ClientMapper mapper;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;

    public ClientServiceImpl(ClientRepository repository,
                             ClientMapper mapper,
                             EmailVerificationService emailVerificationService,
                             PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.mapper = mapper;
        this.emailVerificationService = emailVerificationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ClientResponse create(ClientRequest request) {
        return create(request, true);
    }

    @Override
    public ClientResponse create(ClientRequest request, boolean sendVerification) {
        Client entity = mapper.toEntity(request);
        Client saved = repository.save(entity);
        if (sendVerification && saved.getIdClient() != null) {
            emailVerificationService.sendVerificationForClient(saved.getIdClient());
        }
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClientResponse> findById(Integer id) {
        return repository.findById(id)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ClientResponse> update(Integer id, ClientUpdateRequest request) {
        return repository.findById(id)
                .map(existing -> {
                    if (request.getNom() != null) {
                        existing.setNom(request.getNom().trim());
                    }
                    if (request.getPrenom() != null) {
                        existing.setPrenom(normalizeOptional(request.getPrenom()));
                    }
                    if (request.getEmail() != null) {
                        existing.setEmail(request.getEmail().trim());
                    }
                    if (request.getTelephone() != null) {
                        existing.setTelephone(normalizeOptional(request.getTelephone()));
                    }
                    if (request.getImmatriculation() != null) {
                        existing.setImmatriculation(normalizeOptional(request.getImmatriculation()));
                    }
                    if (request.getVehiculeMarque() != null) {
                        existing.setVehiculeMarque(normalizeOptional(request.getVehiculeMarque()));
                    }
                    if (request.getVehiculeModele() != null) {
                        existing.setVehiculeModele(normalizeOptional(request.getVehiculeModele()));
                    }
                    if (request.getVehiculeEnergie() != null) {
                        existing.setVehiculeEnergie(request.getVehiculeEnergie());
                    }
                    if (request.getAdresseLigne1() != null) {
                        existing.setAdresseLigne1(normalizeOptional(request.getAdresseLigne1()));
                    }
                    if (request.getAdresseLigne2() != null) {
                        existing.setAdresseLigne2(normalizeOptional(request.getAdresseLigne2()));
                    }
                    if (request.getCodePostal() != null) {
                        existing.setAdresseCodePostal(normalizeOptional(request.getCodePostal()));
                    }
                    if (request.getVille() != null) {
                        existing.setAdresseVille(normalizeOptional(request.getVille()));
                    }
                    if (request.getMotDePasse() != null && !request.getMotDePasse().isBlank()) {
                        existing.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
                    }
                    Client updated = repository.save(existing);
                    if (updated.getIdClient() != null) {
                        emailVerificationService.sendVerificationForClient(updated.getIdClient());
                    }
                    return mapper.toResponse(updated);
                });
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public boolean delete(Integer id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}
