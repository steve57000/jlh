package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.PromotionRequest;
import com.jlh.jlhautopambackend.dto.PromotionResponse;
import com.jlh.jlhautopambackend.services.PromotionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService service;

    public PromotionController(PromotionService service) {
        this.service = service;
    }

    @GetMapping
    public List<PromotionResponse> getAll() {
        // Chaque PromotionResponse inclura désormais .getDescription()
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> getById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PromotionResponse> create(
            @RequestPart("data") PromotionRequest req,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        // req.getDescription() est automatiquement renseigné
        PromotionResponse resp = service.create(req, file);
        URI location = URI.create("/api/promotions/" + resp.getIdPromotion());
        return ResponseEntity.created(location).body(resp);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PromotionResponse> update(
            @PathVariable Integer id,
            @RequestPart("data") PromotionRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        // Même chose ici : req.getDescription() contient la nouvelle description
        return service.update(id, req, file)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().build();
    }
}
