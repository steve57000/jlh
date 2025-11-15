package com.jlh.jlhautopambackend.controllers;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.repositories.ClientRepository;

@CrossOrigin
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientRepository repo;

    public ClientController(ClientRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Client> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getById(@PathVariable Integer id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Client> create(@RequestBody Client c) {
        Client saved = repo.save(c);
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> update(@PathVariable Integer id,
                                         @RequestBody Client input) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setNom(input.getNom());
                    existing.setPrenom(input.getPrenom());
                    existing.setEmail(input.getEmail());
                    existing.setTelephone(input.getTelephone());
                    existing.setAdresse(input.getAdresse());
                    return ResponseEntity.ok(repo.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return repo.findById(id)
                .map(e -> {
                    repo.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
