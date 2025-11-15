package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.modeles.DemandeService;
import com.jlh.jlhautopambackend.modeles.DemandeServiceKey;
import com.jlh.jlhautopambackend.repositories.DemandeServiceRepository;
import com.jlh.jlhautopambackend.repositories.DemandeRepository;
import com.jlh.jlhautopambackend.repositories.ServiceRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/demandes-services")
public class DemandeServiceController {

    private final DemandeServiceRepository dsRepo;
    private final DemandeRepository demandeRepo;
    private final ServiceRepository serviceRepo;

    public DemandeServiceController(DemandeServiceRepository dsRepo,
                                    DemandeRepository demandeRepo,
                                    ServiceRepository serviceRepo) {
        this.dsRepo = dsRepo;
        this.demandeRepo = demandeRepo;
        this.serviceRepo = serviceRepo;
    }

    // GET all
    @GetMapping
    public List<DemandeService> getAll() {
        return dsRepo.findAll();
    }

    // GET by composite key
    @GetMapping("/{demandeId}/{serviceId}")
    public ResponseEntity<DemandeService> getById(@PathVariable Integer demandeId,
                                                  @PathVariable Integer serviceId) {
        DemandeServiceKey key = new DemandeServiceKey(demandeId, serviceId);
        return dsRepo.findById(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST create
    @PostMapping
    public ResponseEntity<DemandeService> create(@Valid @RequestBody DemandeService ds) {
        Integer demandeId = ds.getDemande().getIdDemande();
        Integer serviceId = ds.getService().getIdService();

        return demandeRepo.findById(demandeId).flatMap(demande ->
                serviceRepo.findById(serviceId).map(service -> {
                    ds.setDemande(demande);
                    ds.setService(service);
                    ds.setId(new DemandeServiceKey(demandeId, serviceId));
                    DemandeService saved = dsRepo.save(ds);
                    return ResponseEntity
                            .created(URI.create("/api/demandes-services/" + demandeId + "/" + serviceId))
                            .body(saved);
                })
        ).orElseGet(() -> ResponseEntity.badRequest().build());
    }

    // PUT update (only quantite)
    @PutMapping("/{demandeId}/{serviceId}")
    public ResponseEntity<DemandeService> update(@PathVariable Integer demandeId,
                                                 @PathVariable Integer serviceId,
                                                 @Valid @RequestBody DemandeService dto) {
        DemandeServiceKey key = new DemandeServiceKey(demandeId, serviceId);
        return dsRepo.findById(key).map(existing -> {
            existing.setQuantite(dto.getQuantite());
            DemandeService updated = dsRepo.save(existing);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE by composite key
    @DeleteMapping("/{demandeId}/{serviceId}")
    public ResponseEntity<Void> delete(@PathVariable Integer demandeId,
                                       @PathVariable Integer serviceId) {
        DemandeServiceKey key = new DemandeServiceKey(demandeId, serviceId);
        if (!dsRepo.existsById(key)) {
            return ResponseEntity.notFound().build();
        }
        dsRepo.deleteById(key);
        return ResponseEntity.noContent().build();
    }
}
