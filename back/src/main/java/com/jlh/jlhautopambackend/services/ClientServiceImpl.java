package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ClientRequest;
import com.jlh.jlhautopambackend.dto.ClientResponse;
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
    public Optional<ClientResponse> update(Integer id, ClientRequest request) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setNom(request.getNom());
                    existing.setPrenom(request.getPrenom());
                    existing.setEmail(request.getEmail());
                    existing.setTelephone(request.getTelephone());
                    existing.setImmatriculation(request.getImmatriculation());
                    existing.setVehiculeMarque(request.getVehiculeMarque());
                    existing.setVehiculeModele(request.getVehiculeModele());
                    existing.setVehiculeEnergie(request.getVehiculeEnergie());
                    existing.setAdresseLigne1(request.getAdresseLigne1());
                    existing.setAdresseLigne2(request.getAdresseLigne2());
                    existing.setAdresseCodePostal(request.getCodePostal());
                    existing.setAdresseVille(request.getVille());
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

    @Override
    public boolean delete(Integer id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}
